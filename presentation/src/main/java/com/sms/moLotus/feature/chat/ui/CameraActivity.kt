package com.sms.moLotus.feature.chat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sms.moLotus.R

class CameraActivity : AppCompatActivity() {
    val TAG = CameraActivity::class.java.simpleName
    var isRecording = false

    var CAMERA_PERMISSION = Manifest.permission.CAMERA
    var RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    var RC_PERMISSION = 101



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        val recordFiles = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_MOVIES)
        val storageDirectory = recordFiles[0]
        val videoRecordingFilePath = "${storageDirectory.absoluteFile}/${System.currentTimeMillis()}_video.mp4"
        val imageCaptureFilePath = "${storageDirectory.absoluteFile}/${System.currentTimeMillis()}_image.jpg"

        /*if (checkPermissions()) startCameraSession() else requestPermissions()

        imgCapture.setOnLongClickListener {
            if (isRecording) {
                isRecording = false
                Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
                cameraView.stopRecording()
            } else {
                isRecording = true
                Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
                recordVideo(videoRecordingFilePath)
            }
            return@setOnLongClickListener true
        }

        imgCapture.setOnClickListener {
            captureImage(imageCaptureFilePath)
        }*/
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION), RC_PERMISSION)
    }

    private fun checkPermissions(): Boolean {
        return ((ActivityCompat.checkSelfPermission(this, CAMERA_PERMISSION)) == PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(this, CAMERA_PERMISSION)) == PackageManager.PERMISSION_GRANTED)
    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            RC_PERMISSION -> {
                var allPermissionsGranted = false
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    } else {
                        allPermissionsGranted = true
                    }
                }
                if (allPermissionsGranted) startCameraSession() else permissionsNotGranted()
            }
        }
    }

    private fun startCameraSession() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            
            return
        }
        cameraView.bindToLifecycle(this)
    }

    private fun permissionsNotGranted() {
        AlertDialog.Builder(this).setTitle("Permissions required")
            .setMessage("These permissions are required to use this app. Please allow Camera and Audio permissions first")
            .setCancelable(false)
            .setPositiveButton("Grant") { dialog, which -> requestPermissions() }
            .show()
    }

    private fun recordVideo(videoRecordingFilePath: String) {
        cameraView.startRecording(File(videoRecordingFilePath), ContextCompat.getMainExecutor(this), object: VideoCapture.OnVideoSavedCallback {


            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                Toast.makeText(this@CameraActivity, "Recording Saved", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onVideoSaved $videoRecordingFilePath == $outputFileResults")
            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                Toast.makeText(this@CameraActivity, "Recording Failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "onError $videoCaptureError $message")
            }
        })
    }

    private fun captureImage(imageCaptureFilePath: String) {
        cameraView.takePicture(File(imageCaptureFilePath), ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(this@CameraActivity, "Image Captured", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onImageSaved $imageCaptureFilePath")
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@CameraActivity, "Image Capture Failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "onError $exception")
            }
        })
    }*/
}