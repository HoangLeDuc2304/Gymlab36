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

class PoseAnalyzer(
    private val exerciseName: String,
    private val onPoseDetected: (Boolean) -> Unit,
    private val onRepCounted: (Int) -> Unit,
    private val onPoseUpdated: (Pose?) -> Unit = {}
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
    private val STABILITY_THRESHOLD = 3 // Frames needed to change state

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    onPoseUpdated(pose)
                    
                    val rawIsInPose = when {
                        exerciseName.lowercase().contains("plank") -> checkPlankPose(pose)
                        exerciseName.lowercase().contains("lunge") -> checkLungePose(pose)
                        else -> false
                    }
                    
                    // Lọc nhiễu (Stability filter)
                    if (rawIsInPose == lastPoseState) {
                        poseStableCounter++
                    } else {
                        poseStableCounter = 0
                        lastPoseState = rawIsInPose
                    }

                    val isInPose = if (poseStableCounter >= STABILITY_THRESHOLD) rawIsInPose else lastPoseState
                    
                    onPoseDetected(isInPose)
                    
                    // Logic đếm số lần cho Lunge
                    if (exerciseName.lowercase().contains("lunge")) {
                        // Trạng thái Down: Khi người dùng hạ thấp người xuống (isInPose = true)
                        if (isInPose && !isLungeDown) {
                            isLungeDown = true
                        } 
                        // Trạng thái Up: Khi người dùng đứng dậy (isInPose = false)
                        else if (!isInPose && isLungeDown) {
                            isLungeDown = false
                            repCount++
                            onRepCounted(repCount)
                        }
                    }
                }
                .addOnFailureListener {
                    onPoseUpdated(null)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun checkLungePose(pose: Pose): Boolean {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return false

        // Lấy các điểm chính với độ tin cậy > 0.5
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        if (leftHip == null || rightHip == null || leftKnee == null || rightKnee == null || leftAnkle == null || rightAnkle == null) {
            return false
        }
        
        if (leftHip.inFrameLikelihood < 0.5f || rightHip.inFrameLikelihood < 0.5f || 
            leftKnee.inFrameLikelihood < 0.5f || rightKnee.inFrameLikelihood < 0.5f) return false

        val leftKneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle)
        val rightKneeAngle = calculateAngle(rightHip, rightKnee, rightAnkle)

        // Một chân phải gập sâu (Lunge Down phase)
        val isLeftBent = leftKneeAngle in 70.0..120.0
        val isRightBent = rightKneeAngle in 70.0..120.0

        // Kiểm tra độ lệch dọc của đầu gối so với hông (người phải hạ thấp)
        val leftHipToKneeY = abs(leftHip.position.y - leftKnee.position.y)
        val rightHipToKneeY = abs(rightHip.position.y - rightKnee.position.y)
        
        // Khi đứng thẳng, HipToKneeY sẽ lớn. Khi lunge down, nó sẽ nhỏ đi một chút hoặc gối sau chạm đất.
        // Quan trọng nhất là khoảng cách giữa 2 bàn chân (ngang)
        val feetDistanceX = abs(leftAnkle.position.x - rightAnkle.position.x)
        val isSteppingWide = feetDistanceX > 150f 

        return (isLeftBent || isRightBent) && isSteppingWide
    }

    private fun checkPlankPose(pose: Pose): Boolean {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return false

        val shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        if (shoulder == null || hip == null || knee == null || ankle == null) return false
        if (shoulder.inFrameLikelihood < 0.5f || hip.inFrameLikelihood < 0.5f || ankle.inFrameLikelihood < 0.5f) return false

        // 1. Kiểm tra độ thẳng: Góc hông và góc gối
        val hipAngle = calculateAngle(shoulder, hip, knee)
        val kneeAngle = calculateAngle(hip, knee, ankle)

        // Cho phép độ lệch rộng hơn một chút (130 độ thay vì 140-150)
        val isStraight = hipAngle > 130.0 && kneeAngle > 130.0

        // 2. Kiểm tra hướng nằm: Trong Plank, cơ thể phải trải dài theo chiều ngang
        val dx = abs(shoulder.position.x - ankle.position.x)
        val dy = abs(shoulder.position.y - ankle.position.y)
        
        // Tỉ lệ X/Y phải lớn (người nằm ngang). Nếu dy > dx thì khả năng cao là đang đứng.
        val isHorizontal = dx > (dy * 0.5f) // Giảm yêu cầu để dễ nhận diện hơn nếu cam đặt cao

        return isStraight && isHorizontal
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
