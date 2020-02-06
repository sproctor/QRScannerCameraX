package com.natigbabayev.qrscanner

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata


class QrCodeAnalyzer(
    private val onQrCodesDetected: (qrCodes: List<FirebaseVisionBarcode>) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()

            val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

            val rotation = rotationDegreesToFirebaseRotation(image.imageInfo.rotationDegrees)

            val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)

            detector.detectInImage(visionImage)
                .addOnSuccessListener { barcodes ->
                    onQrCodesDetected(barcodes)
                }
                .addOnFailureListener {
                    Log.e("QrCodeAnalyzer", "something went wrong", it)
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }

    private fun rotationDegreesToFirebaseRotation(rotationDegrees: Int): Int {
        return when (rotationDegrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw IllegalArgumentException("Not supported")
        }
    }
}
