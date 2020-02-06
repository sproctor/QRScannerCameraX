package com.natigbabayev.qrscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 10
    }

    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.preview_view)

        // Request camera permissions
        if (isCameraPermissionGranted()) {
            previewView.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val executor = ContextCompat.getMainExecutor(this.applicationContext)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.applicationContext)
        cameraProviderFuture.addListener(Runnable {
            val cameraSelector = CameraSelector.Builder()
                // We want to show input from back camera of the device
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val preview = Preview.Builder().build()

            preview.previewSurfaceProvider = previewView.previewSurfaceProvider

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val qrCodeAnalyzer = QrCodeAnalyzer { qrCodes ->
                qrCodes.forEach {
                    Log.d("MainActivity", "QR Code detected: ${it.rawValue}.")
                }
            }

            imageAnalysis.setAnalyzer(executor, qrCodeAnalyzer)

            // We need to bind preview and imageAnalysis use cases
            CameraX.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, executor)
    }

    private fun isCameraPermissionGranted(): Boolean {
        val selfPermission = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)
        return selfPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (isCameraPermissionGranted()) {
                previewView.post { startCamera() }
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}