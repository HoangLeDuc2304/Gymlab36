const express = require('express');
const mysql = require('mysql2');
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(bodyParser.json());

// --- CẤU HÌNH KẾT NỐI MYSQL WORKBENCH CỦA BẠN ---
const db = mysql.createConnection({
    host: '127.0.0.1',
    user: 'root',
    password: '23042004',
    database: 'gymlab'
});

db.connect(err => {
    if (err) {
        console.error('Lỗi kết nối MySQL: ' + err.message);
        return;
    }
    console.log('Đã kết nối MySQL Workbench thành công!');
});

// Lưu trữ OTP tạm thời (Trong thực tế nên dùng Redis hoặc Table trong DB)
const otpStorage = {};

function getOrCreateSchedule(userId, targetDate, callback) {
    const query = `
        INSERT INTO workout_schedules (user_id, date)
        VALUES (?, ?)
        ON DUPLICATE KEY UPDATE schedule_id = LAST_INSERT_ID(schedule_id)
    `;

    db.execute(query, [userId, targetDate], (err) => {
        if (err) return callback(err);

        db.query('SELECT LAST_INSERT_ID() AS schedule_id', (err2, rows) => {
            if (err2) return callback(err2);
            callback(null, rows[0].schedule_id);
        });
    });
}

// API ĐĂNG NHẬP
app.post('/login', (req, res) => {
    const { email, password } = req.body;
    const query = 'SELECT * FROM users WHERE email = ? AND password = ?';
    db.execute(query, [email, password], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        if (results.length > 0) {
            res.json({ success: true, message: "Thành công", user: results[0] });
        } else {
            res.json({ success: false, message: "Sai tài khoản hoặc mật khẩu" });
        }
    });
});

// API ĐĂNG KÝ
app.post('/register', (req, res) => {
    const { username, email, password, full_name } = req.body;
    const query = 'INSERT INTO users (username, email, password, full_name) VALUES (?, ?, ?, ?)';

    db.execute(query, [username, email, password, full_name], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });

        res.json({
            success: true,
            message: "Đăng ký thành công",
            user: {
                user_id: results.insertId,
                username: username,
                full_name: full_name,
                email: email
            }
        });
    });
});

// --- API QUÊN MẬT KHẨU & OTP ---

// 1. Gửi OTP (Giả lập gửi mail)
app.post('/send-otp', (req, res) => {
    const { email } = req.body;
    if (!email) return res.status(400).json({ success: false, message: "Thiếu email" });

    // Kiểm tra email có tồn tại trong hệ thống không
    db.execute('SELECT * FROM users WHERE email = ?', [email], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        if (results.length === 0) return res.status(404).json({ success: false, message: "Email chưa được đăng ký" });

        // Tạo mã OTP 6 số ngẫu nhiên
        const otp = Math.floor(100000 + Math.random() * 900000).toString();

        // Lưu vào bộ nhớ tạm (Hết hạn sau 5 phút)
        otpStorage[email] = {
            otp: otp,
            expires: Date.now() + 5 * 60 * 1000
        };

        console.log(`[SERVER] Đang gửi OTP ${otp} tới email ${email}`);

        // Trong thực tế bạn sẽ dùng thư viện 'nodemailer' để gửi mail thật ở đây.
        res.json({
            success: true,
            message: "Mã OTP đã được gửi (Giả lập). Hãy kiểm tra console của Server!"
        });
    });
});

// 2. Đặt lại mật khẩu
app.post('/reset-password', (req, res) => {
    const { email, otp, newPassword } = req.body;

    if (!email || !otp || !newPassword) {
        return res.status(400).json({ success: false, message: "Thiếu thông tin" });
    }

    const record = otpStorage[email];

    // Kiểm tra mã OTP
    if (!record || record.otp !== otp) {
        return res.status(400).json({ success: false, message: "Mã OTP không chính xác" });
    }

    // Kiểm tra hết hạn
    if (Date.now() > record.expires) {
        delete otpStorage[email];
        return res.status(400).json({ success: false, message: "Mã OTP đã hết hạn" });
    }

    // Cập nhật mật khẩu trong Database
    const query = 'UPDATE users SET password = ? WHERE email = ?';
    db.execute(query, [newPassword, email], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });

        // Xóa OTP sau khi dùng xong
        delete otpStorage[email];

        res.json({ success: true, message: "Mật khẩu đã được cập nhật thành công!" });
    });
});

// --- API THỰC ĐƠN ---
app.get('/diet/:day', (req, res) => {
    const day = req.params.day;
    const query = 'SELECT * FROM diet_suggestions WHERE meal_type = ?';
    db.execute(query, [day], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, data: results });
    });
});

app.post('/diet/add', (req, res) => {
    const { title, calories, meal_type, user_id } = req.body;
    const query = 'INSERT INTO diet_suggestions (title, calories, meal_type, user_id) VALUES (?, ?, ?, ?)';
    db.execute(query, [title, calories, meal_type, user_id || 1], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, message: "Đã lưu vào MySQL!" });
    });
});

app.delete('/diet/:id', (req, res) => {
    const id = req.params.id;
    const query = 'DELETE FROM diet_suggestions WHERE suggestion_id = ?';
    db.execute(query, [id], (err, result) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, message: 'Đã xóa món ăn!' });
    });
});

// --- API CÂN NẶNG ---
app.get('/weight/history/:userId', (req, res) => {
    const userId = req.params.userId;
    const query = 'SELECT * FROM weight_history WHERE user_id = ? ORDER BY recorded_date DESC, id DESC';
    db.execute(query, [userId], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, data: results });
    });
});

app.post('/weight/add', (req, res) => {
    const { weight, user_id } = req.body;
    const recorded_date = new Date().toISOString().slice(0, 10);
    const query = 'INSERT INTO weight_history (user_id, weight, recorded_date) VALUES (?, ?, ?)';
    db.execute(query, [user_id || 1, weight, recorded_date], (err, results) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, message: "Đã lưu cân nặng thành công!" });
    });
});

app.get('/categories', (req, res) => {
    const query = 'SELECT id, name, description, image_url FROM categories ORDER BY id ASC';
    db.execute(query, [], (err, results) => {
        if (err) return res.status(500).json({ message: err.message });
        res.json(results);
    });
});

app.get('/exercises', (req, res) => {
    const { limit, category_id } = req.query;
    let query = 'SELECT e.exercise_id AS id, e.name, e.description, e.video_url, e.calories, e.difficulty_level, e.duration_seconds, e.category_id, c.name AS category_name FROM exercises e LEFT JOIN categories c ON e.category_id = c.id';
    const params = [];
    if (category_id) { query += ' WHERE e.category_id = ?'; params.push(Number(category_id)); }
    query += ' ORDER BY e.exercise_id ASC';
    if (limit) { query += ' LIMIT ?'; params.push(Number(limit)); }
    db.execute(query, params, (err, results) => {
        if (err) return res.status(500).json({ message: err.message });
        res.json(results);
    });
});

app.get('/templates', (req, res) => {
    const limit = req.query.limit ? Number(req.query.limit) : null;
    let query = 'SELECT id, name, description, is_global, user_id, created_at FROM workout_templates WHERE is_global = 1 OR user_id = 1 ORDER BY id DESC';
    if (limit) query += ` LIMIT ${limit}`;
    db.query(query, (err, results) => {
        if (err) return res.status(500).json({ message: err.message });
        res.json(results);
    });
});

app.post('/apply-template', (req, res) => {
    const { user_id, target_date, template_id } = req.body;
    getOrCreateSchedule(user_id, target_date, (err, scheduleId) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        const insertFromTemplate = 'INSERT INTO session_exercise_details (schedule_id, exercise_id, sets, reps, duration_actual, calories_burned, is_completed, order_index) SELECT ?, exercise_id, sets, reps, 0, 0, 0, order_index FROM template_exercises WHERE template_id = ?';
        db.execute(insertFromTemplate, [scheduleId, template_id], (err2) => {
            if (err2) return res.status(500).json({ success: false, message: err2.message });
            res.json({ success: true, message: 'Đã áp dụng mẫu lịch tập!' });
        });
    });
});

app.get('/daily-schedule', (req, res) => {
    const { user_id, date } = req.query;
    const findSchedule = 'SELECT schedule_id FROM workout_schedules WHERE user_id = ? AND date = ? LIMIT 1';
    db.execute(findSchedule, [user_id, date], (err, schedules) => {
        if (err || schedules.length === 0) return res.json({ totalCalories: 0, completedCount: 0, totalCount: 0, exercises: [] });
        const detailsQuery = 'SELECT sed.detail_id AS detailId, e.name, sed.is_completed AS isCompleted, e.duration_seconds AS duration, COALESCE(sed.calories_burned, e.calories) AS calories FROM session_exercise_details sed JOIN exercises e ON sed.exercise_id = e.exercise_id WHERE sed.schedule_id = ?';
        db.execute(detailsQuery, [schedules[0].schedule_id], (err2, rows) => {
            if (err2) return res.status(500).json({ success: false, message: err2.message });
            res.json({ totalCalories: rows.reduce((s, i) => s + i.calories, 0), completedCount: rows.filter(i => i.isCompleted).length, totalCount: rows.length, exercises: rows });
        });
    });
});

app.post('/complete-exercise', (req, res) => {
    const { detail_id, duration, mode, effort_feedback } = req.body;
    const updateDetail = 'UPDATE session_exercise_details SET duration_actual = ?, is_completed = 1, workout_mode = ?, effort_feedback = ? WHERE detail_id = ?';
    db.execute(updateDetail, [duration, mode || 'normal', effort_feedback || 0, detail_id], (err) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, message: 'Hoàn thành bài tập!' });
    });
});

app.get('/achievements/:userId', (req, res) => {
    db.execute('SELECT * FROM achievements WHERE user_id = ? LIMIT 1', [req.params.userId], (err, rows) => {
        if (err) return res.status(500).json({ success: false, message: err.message });
        res.json({ success: true, stats: rows[0] || {} });
    });
});

app.listen(3000, '0.0.0.0', () => {
    console.log('Server Gymlab đang chạy tại http://0.0.0.0:3000');
});