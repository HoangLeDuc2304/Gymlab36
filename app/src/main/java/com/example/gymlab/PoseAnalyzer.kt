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
    private val onPoseUpdated: (Pose?, Int, Int) -> Unit = { _, _, _ -> }
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
    private val STABILITY_THRESHOLD = 2 

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
            
            // Lấy kích thước ảnh thực tế sau khi đã tính đến rotation
            val isRotated = rotationDegrees == 90 || rotationDegrees == 270
            val width = if (isRotated) imageProxy.height else imageProxy.width
            val height = if (isRotated) imageProxy.width else imageProxy.height

            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    onPoseUpdated(pose, width, height)
                    
                    val rawIsInPose = when {
                        exerciseName.lowercase().contains("plank") -> checkPlankPose(pose)
                        exerciseName.lowercase().contains("lunge") -> checkLungePose(pose)
                        else -> false
                    }
                    
                    if (rawIsInPose == lastPoseState) {
                        poseStableCounter++
                    } else {
                        poseStableCounter = 0
                        lastPoseState = rawIsInPose
                    }

                    val isInPose = if (poseStableCounter >= STABILITY_THRESHOLD) rawIsInPose else lastPoseState
                    
                    onPoseDetected(isInPose)
                    
                    if (exerciseName.lowercase().contains("lunge")) {
                        if (isInPose && !isLungeDown) {
                            isLungeDown = true
                        } 
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

    private fun checkLungePose(pose: Pose): Boolean {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return false

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

        val isLeftBent = leftKneeAngle in 70.0..120.0
        val isRightBent = rightKneeAngle in 70.0..120.0

        val feetDistanceX = abs(leftAnkle.position.x - rightAnkle.position.x)
        val isSteppingWide = feetDistanceX > 150f 

        return (isLeftBent || isRightBent) && isSteppingWide
    }

    private fun checkPlankPose(pose: Pose): Boolean {
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

        if (shoulder == null || hip == null || knee == null || ankle == null) return false
        
        val minConfidence = 0.4f
        if (shoulder.inFrameLikelihood < minConfidence || hip.inFrameLikelihood < minConfidence || ankle.inFrameLikelihood < minConfidence) return false

        val hipAngle = calculateAngle(shoulder, hip, knee)
        val kneeAngle = calculateAngle(hip, knee, ankle)
        
        val isStraight = hipAngle > 120.0 && kneeAngle > 120.0

        val dx = abs(shoulder.position.x - ankle.position.x)
        val dy = abs(shoulder.position.y - ankle.position.y)
        
        val isHorizontal = dx > dy

        val shoulderHipDY = abs(shoulder.position.y - hip.position.y)
        val isFlat = shoulderHipDY < 250f

        return isStraight && isHorizontal && isFlat
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
