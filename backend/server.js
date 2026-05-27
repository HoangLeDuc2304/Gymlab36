const express = require('express');
const mysql = require('mysql2');
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();
app.use(cors());

// Tăng giới hạn dung lượng body để nhận được ảnh Base64 lớn (ví dụ: 50MB)
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true }));

// --- CẤU HÌNH KẾT NỐI MYSQL WORKBENCH CỦA BẠN ---
const db = mysql.createConnection({
    host: '127.0.0.1',
    port: 3306,
    user: 'root',
    password: 'thuylinh2812',
    database: 'gymlab'
});

db.connect(err => {
    if (err) {
        console.error('Lỗi kết nối MySQL: ' + err.message);
        return;
    }
    console.log('Đã kết nối MySQL Workbench thành công (Cổng 3306)!');
});

// --- API NGƯỜI DÙNG & AUTH ---
app.get('/users/:userId', (req, res) => {
    db.query('SELECT * FROM users WHERE user_id = ?', [req.params.userId], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, user: results[0] });
    });
});

app.post('/login', (req, res) => {
    const { email, password } = req.body;
    db.query('SELECT * FROM users WHERE email = ? AND password = ?', [email, password], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        if (results.length > 0) res.json({ success: true, message: "Thành công", user: results[0] });
        else res.json({ success: false, message: "Sai tài khoản hoặc mật khẩu" });
    });
});

app.post('/register', (req, res) => {
    const { username, email, password, full_name } = req.body;
    db.query('INSERT INTO users (username, email, password, full_name) VALUES (?, ?, ?, ?)', [username, email, password, full_name], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, user: { user_id: results.insertId, username, email } });
    });
});

app.post('/users/update-name', (req, res) => {
    const { user_id, username } = req.body;
    db.query('UPDATE users SET username = ? WHERE user_id = ?', [username, user_id], (err) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true });
    });
});

app.post('/users/update-avatar', (req, res) => {
    const { user_id, profile_image } = req.body;
    db.query('UPDATE users SET profile_image = ? WHERE user_id = ?', [profile_image, user_id], (err) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, message: "Cập nhật ảnh đại diện thành công!" });
    });
});

app.post('/change-password', (req, res) => {
    const { email, oldPassword, newPassword } = req.body;
    db.query('SELECT * FROM users WHERE email = ? AND password = ?', [email, oldPassword], (err, results) => {
        if (err || results.length === 0) return res.json({ success: false, message: "Mật khẩu hiện tại không đúng" });
        db.query('UPDATE users SET password = ? WHERE email = ?', [newPassword, email], (err2) => {
            if (err2) return res.status(500).json({ success: false, message: err2.message });
            res.json({ success: true });
        });
    });
});

// --- API THỰC ĐƠN (DIET) ---
app.get('/diet/:day', (req, res) => {
    db.query('SELECT * FROM diet_suggestions WHERE day_of_week = ?', [req.params.day], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, data: results });
    });
});

app.post('/diet/add', (req, res) => {
    const { title, calories, meal_type, day_of_week, user_id, thumbnail_url } = req.body;
    const query = 'INSERT INTO diet_suggestions (title, calories, meal_type, day_of_week, user_id, thumbnail_url) VALUES (?, ?, ?, ?, ?, ?)';
    db.query(query, [title, calories, meal_type, day_of_week, user_id || 1, thumbnail_url || null], (err) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, message: "Đã lưu thực đơn!" });
    });
});

app.delete('/diet/:id', (req, res) => {
    db.query('DELETE FROM diet_suggestions WHERE suggestion_id = ?', [req.params.id], (err) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true });
    });
});

// --- API CÂN NẶNG (WEIGHT) ---
app.get('/weight/history/:userId', (req, res) => {
    const query = "SELECT id, user_id, weight, DATE_FORMAT(recorded_date, '%Y-%m-%d') as recorded_date FROM weight_history WHERE user_id = ? ORDER BY recorded_date DESC, id DESC";
    db.query(query, [req.params.userId], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, data: results });
    });
});

app.post('/weight/add', (req, res) => {
    const { weight, user_id } = req.body;
    const recorded_date = new Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Ho_Chi_Minh' }).format(new Date());
    db.query('INSERT INTO weight_history (user_id, weight, recorded_date) VALUES (?, ?, ?)', [user_id || 1, weight, recorded_date], (err) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true });
    });
});

// --- API LỊCH TẬP & BÀI TẬP ---
app.get('/daily-schedule', (req, res) => {
    const { user_id, date } = req.query;
    const query = `
        SELECT sed.detail_id AS detailId, e.name, sed.is_completed AS isCompleted,
        e.duration AS duration, e.calories
        FROM session_exercise_details sed
        JOIN exercises e ON sed.exercise_id = e.exercise_id
        JOIN workout_schedules ws ON sed.schedule_id = ws.schedule_id
        WHERE ws.user_id = ? AND ws.date = ?
        ORDER BY sed.order_index ASC
    `;
    db.query(query, [user_id, date], (err, rows) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        const exercises = rows.map(r => ({ ...r, isCompleted: !!r.isCompleted }));
        const completedCount = exercises.filter(ex => ex.isCompleted).length;
        const totalCalories = exercises.reduce((sum, ex) => sum + (ex.isCompleted ? Number(ex.calories || 0) : 0), 0);
        res.json({ success: true, totalCalories, completedCount, totalCount: exercises.length, exercises });
    });
});

app.get('/exercises', (req, res) => {
    const { category_id } = req.query;
    let query = 'SELECT exercise_id AS id, name, description, duration, calories, category_name, category_id FROM exercises';
    const params = [];
    if (category_id && category_id !== 'null') {
        query += ' WHERE category_id = ?';
        params.push(Number(category_id));
    }
    db.query(query, params, (err, results) => {
        if (err) return res.status(500).json({ message: err.message });
        res.json(results);
    });
});

app.get('/categories', (req, res) => {
    db.query('SELECT * FROM categories', (err, results) => {
        if (err) return res.status(500).json({ message: err.message });
        res.json(results);
    });
});

app.get('/templates', (req, res) => {
    const { user_id } = req.query;
    // LẤY TẤT CẢ: Global (user_id IS NULL) HOẶC của chính User đó
    const query = 'SELECT * FROM workout_templates WHERE is_global = 1 OR user_id = ? OR user_id IS NULL ORDER BY id DESC';
    db.query(query, [user_id || 0], (err, results) => {
        if (err) return res.status(500).json({ message: err.message });
        res.json(results);
    });
});

app.post('/complete-exercise', (req, res) => {
    const { detail_id, duration, mode, effort_feedback } = req.body;
    const findQuery = `
        SELECT sed.*, e.calories, e.duration AS base_duration, ws.user_id
        FROM session_exercise_details sed
        JOIN exercises e ON sed.exercise_id = e.exercise_id
        JOIN workout_schedules ws ON sed.schedule_id = ws.schedule_id
        WHERE sed.detail_id = ?
    `;
    db.query(findQuery, [detail_id], (err, results) => {
        if (err || results.length === 0) return res.status(500).json({ success: false, message: "Không tìm thấy bài tập" });
        const row = results[0];
        const baseSeconds = parseInt(row.base_duration) || 60;
        let finalCalories = Math.round((duration / baseSeconds) * row.calories);
        if (effort_feedback === -1) finalCalories *= 0.9;
        if (effort_feedback === 1) finalCalories *= 1.15;
        finalCalories = Math.round(finalCalories);

        const updateDetail = `
            UPDATE session_exercise_details
            SET duration_actual = ?, calories_burned = ?, is_completed = 1, workout_mode = ?, effort_feedback = ?
            WHERE detail_id = ?
        `;
        db.query(updateDetail, [duration, finalCalories, mode || 'normal', effort_feedback, detail_id], (err2) => {
            if (err2) return res.status(500).json({ success: false, message: err2.message });
            const updateAch = `
                INSERT INTO achievements (user_id, total_exp, total_points, last_workout_date)
                VALUES (?, ?, ?, CURDATE())
                ON DUPLICATE KEY UPDATE
                total_exp = total_exp + VALUES(total_exp),
                total_points = total_points + VALUES(total_points),
                last_workout_date = CURDATE()
            `;
            db.query(updateAch, [row.user_id, finalCalories, finalCalories], (err3) => {
                res.json({ success: true, message: "Đã lưu kết quả!", calories: finalCalories, exp: finalCalories });
            });
        });
    });
});

// --- API THÀNH TÍCH (ACHIEVEMENTS) ---
app.get('/achievements/:userId', (req, res) => {
    const userId = req.params.userId;
    db.query('SELECT * FROM achievements WHERE user_id = ? LIMIT 1', [userId], (err, statsRows) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        const stats = statsRows.length > 0 ? statsRows[0] : { level: 1, total_exp: 0, current_streak: 0, longest_streak: 0, total_points: 0 };
        db.query('SELECT * FROM badges', (err2, badgesRows) => {
            if (err2) return res.status(500).json({ success: false, message: err2.message });
            res.json({ success: true, stats: stats, badges: badgesRows });
        });
    });
});

app.listen(3001, '0.0.0.0', () => {
    console.log('Server Gymlab đang chạy tại http://10.0.2.2:3001');
});
