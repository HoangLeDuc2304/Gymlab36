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

    private var isLungeDown = false
    private var repCount = 0
    private var lastPoseState = false
    private var poseStableCounter = 0
    
    private val STABILITY_THRESHOLD = 5 
    private val UP_THRESHOLD_ANGLE = 150.0 
    private val DOWN_THRESHOLD_ANGLE = 110.0 

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
                    val result = if (exerciseName.lowercase().contains("plank")) {
                        checkPlankPoseWithAccuracy(pose)
                    } else {
                        checkLungePoseWithAccuracy(pose)
                    }
                    
                    val rawIsInPose = result.first
                    accuracy = result.second
                    feedback = result.third
                    
                    onAccuracyUpdated(accuracy)
                    onFeedbackUpdated(feedback)
                    
                    if (rawIsInPose == lastPoseState) {
                        poseStableCounter++
                    } else {
                        poseStableCounter = 0
                        lastPoseState = rawIsInPose
                    }

                    val isInPose = if (poseStableCounter >= STABILITY_THRESHOLD) rawIsInPose else lastPoseState
                    onPoseDetected(isInPose)
                    
                    if (exerciseName.lowercase().contains("lunge")) {
                        val kneeAngles = getKneeAngles(pose)
                        if (kneeAngles != null) {
                            val minKneeAngle = min(kneeAngles.first, kneeAngles.second)
                            
                            if (minKneeAngle < DOWN_THRESHOLD_ANGLE && !isLungeDown) {
                                isLungeDown = true
                            } 
                            else if (minKneeAngle > UP_THRESHOLD_ANGLE && isLungeDown) {
                                isLungeDown = false
                                repCount++
                                onRepCounted(repCount)
                            }
                        }
                    }
                }
                .addOnFailureListener { onPoseUpdated(null, width, height) }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun getKneeAngles(pose: Pose): Pair<Double, Double>? {
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        
        if (leftHip == null || rightHip == null || leftKnee == null || rightKnee == null || leftAnkle == null || rightAnkle == null) return null
        
        return Pair(calculateAngle(leftHip, leftKnee, leftAnkle), calculateAngle(rightHip, rightKnee, rightAnkle))
    }

    private fun checkLungePoseWithAccuracy(pose: Pose): Triple<Boolean, Int, String> {
        val angles = getKneeAngles(pose) ?: return Triple(false, 0, "Không tìm thấy người")
        
        val frontKneeAngle = min(angles.first, angles.second)
        val backKneeAngle = max(angles.first, angles.second)

        val frontScore = max(0.0, 100.0 - abs(frontKneeAngle - 90.0) * 2.0)
        val backScore = max(0.0, 100.0 - abs(backKneeAngle - 90.0) * 2.0)
        val currentAccuracy = ((frontScore + backScore) / 2.0).toInt()

        var feedback = "Tư thế tốt!"
        if (frontKneeAngle > 110) feedback = "Gập gối trước sâu hơn"
        else if (backKneeAngle > 110) feedback = "Hạ gối sau thấp xuống"

        val isInPose = frontKneeAngle in 75.0..105.0 && backKneeAngle in 75.0..110.0
        return Triple(isInPose, if (isInPose) currentAccuracy else 0, feedback)
    }

    private fun checkPlankPoseWithAccuracy(pose: Pose): Triple<Boolean, Int, String> {
        val shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        if (shoulder == null || hip == null || knee == null || ankle == null) return Triple(false, 0, "Hãy nằm ngang")
        
        val hipAngle = calculateAngle(shoulder, hip, knee)
        val kneeAngle = calculateAngle(hip, knee, ankle)
        val currentAccuracy = (((hipAngle - 120.0) / 60.0 * 100.0 + (kneeAngle - 120.0) / 60.0 * 100.0) / 2.0).toInt()
        val isInPose = hipAngle > 130.0 && kneeAngle > 140.0
        return Triple(isInPose, if (isInPose) currentAccuracy else 0, "Giữ thẳng người")
    }

    private fun calculateAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        var result = Math.toDegrees(
            atan2(lastPoint.position.y - midPoint.position.y, lastPoint.position.x - midPoint.position.x).toDouble()
                    - atan2(firstPoint.position.y - midPoint.position.y, firstPoint.position.x - midPoint.position.x).toDouble()
        )
        result = abs(result)
        if (result > 180) result = 360.0 - result
        return result
    }
}