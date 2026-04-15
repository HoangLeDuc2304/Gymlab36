package com.example.gymlab

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class PoseAnalyzer(
    private val exerciseName: String,
    private val onPoseDetected: (Boolean) -> Unit,
    private val onRepCounted: (Int) -> Unit,
    private val onPoseUpdated: (Pose?, Int, Int) -> Unit = { _, _, _ -> },
    private val onAccuracyUpdated: (Int) -> Unit = {},
    private val onFeedbackUpdated: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {

    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)

    // State for counting reps
    private var isLungeDown = false
    private var repCount = 0
    private var lastPoseState = false
    private var poseStableCounter = 0
    private val STABILITY_THRESHOLD = 5 // Tăng độ ổn định lên 5 frames để loại bỏ jitter

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
            
            val isRotated = rotationDegrees == 90 || rotationDegrees == 270
            val width = if (isRotated) imageProxy.height else imageProxy.width
            val height = if (isRotated) imageProxy.width else imageProxy.height

            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    onPoseUpdated(pose, width, height)
                    
                    var accuracy = 0
                    var feedback = ""
                    val rawIsInPose = when {
                        exerciseName.lowercase().contains("plank") -> {
                            val result = checkPlankPoseWithAccuracy(pose)
                            accuracy = result.second
                            feedback = result.third
                            result.first
                        }
                        exerciseName.lowercase().contains("lunge") -> {
                            val result = checkLungePoseWithAccuracy(pose)
                            accuracy = result.second
                            feedback = result.third
                            result.first
                        }
                        else -> false
                    }
                    
                    onAccuracyUpdated(accuracy)
                    onFeedbackUpdated(feedback)
                    
                    if (rawIsInPose == lastPoseState) {
                        poseStableCounter++
                    } else {
                        poseStableCounter = 0
                        lastPoseState = rawIsInPose
                    }

                    // Chỉ coi là "đang trong tư thế" nếu nó ổn định
                    val isInPose = if (poseStableCounter >= STABILITY_THRESHOLD) rawIsInPose else lastPoseState
                    
                    onPoseDetected(isInPose)
                    
                    // Logic đếm Rep: Yêu cầu chu kỳ UP -> DOWN -> UP rõ ràng
                    if (exerciseName.lowercase().contains("lunge")) {
                        // Xuống sâu: Trạng thái Down
                        if (isInPose && !isLungeDown) {
                            isLungeDown = true
                        } 
                        // Lên hoàn toàn: Kết thúc 1 Rep
                        else if (!isInPose && isLungeDown) {
                            isLungeDown = false
                            repCount++
                            onRepCounted(repCount)
                        }
                    }
                }
                .addOnFailureListener {
                    onPoseUpdated(null, width, height)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun checkLungePoseWithAccuracy(pose: Pose): Triple<Boolean, Int, String> {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return Triple(false, 0, "Không tìm thấy người")

        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        if (leftHip == null || rightHip == null || leftKnee == null || rightKnee == null || leftAnkle == null || rightAnkle == null) {
            return Triple(false, 0, "Hãy đứng xa hơn để thấy toàn thân")
        }
        
        // Độ tin cậy cao hơn để tránh nhảy điểm
        if (leftHip.inFrameLikelihood < 0.7f || rightHip.inFrameLikelihood < 0.7f || 
            leftKnee.inFrameLikelihood < 0.7f || rightKnee.inFrameLikelihood < 0.7f) 
            return Triple(false, 0, "Hãy đứng ở nơi đủ ánh sáng")

        val leftKneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle)
        val rightKneeAngle = calculateAngle(rightHip, rightKnee, rightAnkle)

        // Phân biệt chân trước/sau
        val isLeftFront = leftHip.position.y > rightHip.position.y // Chỉ là cách heuristic dựa trên chiều dọc
        val frontKneeAngle = if (leftHip.position.y < rightHip.position.y) leftKneeAngle else rightKneeAngle
        val backKneeAngle = if (leftHip.position.y < rightHip.position.y) rightKneeAngle else leftKneeAngle

        // Logic đếm: Cả 2 gối đều phải ở vùng gập (Down phase)
        // Lunge chuẩn cần: gối trước ~ 90độ, gối sau ~ 90 độ
        val isFrontBent = frontKneeAngle in 75.0..105.0
        val isBackBent = backKneeAngle in 75.0..110.0
        
        // Kiểm tra trọng tâm: Hông phải hạ thấp đáng kể so với lúc đứng (cần thêm logic này nếu muốn siêu chuẩn)
        val feetDistanceX = abs(leftAnkle.position.x - rightAnkle.position.x)
        val isSteppingWide = feetDistanceX > 180f 

        val isInPose = isFrontBent && isBackBent && isSteppingWide
        
        // Tính điểm: càng gần 90 cho cả 2 gối thì điểm càng cao
        val frontScore = max(0.0, 100.0 - abs(frontKneeAngle - 90.0) * 2.0)
        val backScore = max(0.0, 100.0 - abs(backKneeAngle - 90.0) * 2.0)
        val currentAccuracy = ((frontScore + backScore) / 2.0).toInt()

        var feedback = if (isInPose) "Tư thế tốt!" else "Hãy giữ lưng thẳng, gập gối 90 độ"
        if (!isFrontBent) feedback = "Gập gối trước 90 độ"
        if (!isBackBent) feedback = "Hạ thấp gối sau"
        if (!isSteppingWide) feedback = "Bước chân rộng hơn"

        return Triple(isInPose, if (isInPose) currentAccuracy else 0, feedback)
    }

    private fun checkPlankPoseWithAccuracy(pose: Pose): Triple<Boolean, Int, String> {
        fun getBestLandmark(leftType: Int, rightType: Int): PoseLandmark? {
            val left = pose.getPoseLandmark(leftType)
            val right = pose.getPoseLandmark(rightType)
            return when {
                left == null -> right
                right == null -> left
                left.inFrameLikelihood > right.inFrameLikelihood -> left
                else -> right
            }
        }

        val shoulder = getBestLandmark(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)
        val hip = getBestLandmark(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
        val knee = getBestLandmark(PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE)
        val ankle = getBestLandmark(PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE)

        if (shoulder == null || hip == null || knee == null || ankle == null) return Triple(false, 0, "Hãy nằm ngang trước camera")
        
        val minConfidence = 0.6f
        if (shoulder.inFrameLikelihood < minConfidence || hip.inFrameLikelihood < minConfidence || ankle.inFrameLikelihood < minConfidence) return Triple(false, 0, "Hãy đứng ở nơi đủ ánh sáng")

        val hipAngle = calculateAngle(shoulder, hip, knee)
        val kneeAngle = calculateAngle(hip, knee, ankle)
        
        val hipScore = max(0.0, (min(hipAngle, 180.0) - 120.0) / 60.0 * 100.0)
        val kneeScore = max(0.0, (min(kneeAngle, 180.0) - 120.0) / 60.0 * 100.0)
        val currentAccuracy = ((hipScore + kneeScore) / 2.0).toInt()

        val isStraight = hipAngle > 130.0 && kneeAngle > 140.0 // Siết chặt plank hơn

        val dx = abs(shoulder.position.x - ankle.position.x)
        val dy = abs(shoulder.position.y - ankle.position.y)
        val isHorizontal = dx > dy * 0.8

        val shoulderHipDY = abs(shoulder.position.y - hip.position.y)
        val isFlat = shoulderHipDY < 150f // Mông không được cao quá

        var feedback = "Tư thế tốt!"
        if (hipAngle < 130) feedback = "Đừng đẩy mông cao quá"
        else if (hipAngle > 190) feedback = "Đừng để võng lưng"
        else if (dy > dx) feedback = "Hãy nằm ngang người"

        val isInPose = isStraight && isHorizontal && isFlat
        return Triple(isInPose, if (isInPose) currentAccuracy else 0, feedback)
    }

    private fun calculateAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        var result = Math.toDegrees(
            atan2(lastPoint.position.y - midPoint.position.y, lastPoint.position.x - midPoint.position.x).toDouble()
                    - atan2(firstPoint.position.y - midPoint.position.y, firstPoint.position.x - midPoint.position.x).toDouble()
        )
        result = abs(result)
        if (result > 180) {
            result = 360.0 - result
        }
        return result
    }
}
