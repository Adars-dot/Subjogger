package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

data class CropBounds(
    val leftFraction: Float = 0.06f,
    val topFraction: Float = 0.08f,
    val rightFraction: Float = 0.94f,
    val bottomFraction: Float = 0.92f
)

data class PipelineAuditMetrics(
    val originalWidth: Int,
    val originalHeight: Int,
    val sanitizedWidth: Int,
    val sanitizedHeight: Int,
    val originalKb: Int,
    val sanitizedKb: Int,
    val exifStripped: Boolean = true
)

data class OcrPipelineResult(
    val recognizedText: String,
    val audit: PipelineAuditMetrics
)

object MlKitOcrEngine {

    suspend fun inspectOriginalBounds(context: Context, uri: Uri): PipelineAuditMetrics? =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts)
            val origW = max(1, opts.outWidth)
            val origH = max(1, opts.outHeight)
            val origKb = max(1, rawBytes.size / 1024)
            val scale = minOf(1080f / origW, 1600f / origH, 1f)
            val targetW = max(1, (origW * scale).roundToInt())
            val targetH = max(1, (origH * scale).roundToInt())
            PipelineAuditMetrics(
                originalWidth = origW,
                originalHeight = origH,
                sanitizedWidth = targetW,
                sanitizedHeight = targetH,
                originalKb = origKb,
                sanitizedKb = max(24, (origKb * 0.25f).roundToInt()),
                exifStripped = true
            )
        }

    suspend fun sanitizeCropAndRecognize(
        context: Context,
        uri: Uri,
        crop: CropBounds
    ): OcrPipelineResult = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Unable to read selected screenshot.")

        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOpts)
        val origW = max(1, boundsOpts.outWidth)
        val origH = max(1, boundsOpts.outHeight)
        val origKb = max(1, rawBytes.size / 1024)

        var sampleSize = 1
        while (origW / sampleSize > 1600 || origH / sampleSize > 2200) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
            ?: throw IllegalStateException("Could not decode selected image.")

        val cropX = (crop.leftFraction.coerceIn(0f, 0.8f) * decoded.width).roundToInt()
        val cropY = (crop.topFraction.coerceIn(0f, 0.8f) * decoded.height).roundToInt()
        val cropW = max(32, ((crop.rightFraction - crop.leftFraction).coerceIn(0.15f, 1f) * decoded.width).roundToInt())
            .coerceAtMost(decoded.width - cropX)
        val cropH = max(32, ((crop.bottomFraction - crop.topFraction).coerceIn(0.15f, 1f) * decoded.height).roundToInt())
            .coerceAtMost(decoded.height - cropY)

        val cropped = Bitmap.createBitmap(decoded, cropX, cropY, cropW, cropH)

        // Downscale to <= 1080x1600
        val scale = minOf(1080f / cropped.width, 1600f / cropped.height, 1f)
        val targetW = max(1, (cropped.width * scale).roundToInt())
        val targetH = max(1, (cropped.height * scale).roundToInt())
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
        } else {
            cropped
        }

        // Re-encode into clean PNG-24 buffer to guarantee zero EXIF/GPS/device metadata
        val pngStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 100, pngStream)
        val cleanBytes = pngStream.toByteArray()
        val cleanBitmap = BitmapFactory.decodeByteArray(cleanBytes, 0, cleanBytes.size)
            ?: scaled

        val recognized = recognizeTextWithMlKit(cleanBitmap)

        if (decoded !== cropped) decoded.recycle()
        if (cropped !== scaled) cropped.recycle()

        OcrPipelineResult(
            recognizedText = recognized,
            audit = PipelineAuditMetrics(
                originalWidth = origW,
                originalHeight = origH,
                sanitizedWidth = cleanBitmap.width,
                sanitizedHeight = cleanBitmap.height,
                originalKb = origKb,
                sanitizedKb = max(1, cleanBytes.size / 1024),
                exifStripped = true
            )
        )
    }

    private suspend fun recognizeTextWithMlKit(bitmap: Bitmap): String =
        suspendCancellableCoroutine { cont ->
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    recognizer.close()
                    cont.resume(result.text)
                }
                .addOnFailureListener { error ->
                    recognizer.close()
                    cont.resumeWithException(error)
                }
        }
}
