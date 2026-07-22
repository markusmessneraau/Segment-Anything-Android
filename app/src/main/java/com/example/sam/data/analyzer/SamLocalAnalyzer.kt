package com.example.sam.data.analyzer

import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.example.sam.data.model.TapPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import java.nio.IntBuffer


class SamLocalAnalyzer(private val context: Context) {

    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var encoderSession: OrtSession? = null
    private var decoderSession: OrtSession? = null

    // Speicher für das aktuell geladene Bild und Merkmale
    private var currentResizedBitmap: Bitmap? = null
    private var imageEmbed: OnnxTensor? = null
    private var highResFeats0: OnnxTensor? = null
    private var highResFeats1: OnnxTensor? = null

    init {
        loadModels()
    }

    private fun loadModels() {
        try {
            val sessionOptions = OrtSession.SessionOptions().apply {
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
                addConfigEntry("session.use_device_allocator_for_initializers", "1")
            }
            val encoderFile = copyAssetToCache("sam2_hiera_tiny_encoder.onnx")
            val decoderFile = copyAssetToCache("sam2_hiera_tiny_decoder.onnx")

            encoderSession = ortEnv.createSession(encoderFile.absolutePath, sessionOptions)
            decoderSession = ortEnv.createSession(decoderFile.absolutePath, sessionOptions)
            println("SAM 2: Modelle einsatzbereit! ")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyAssetToCache(fileName: String): File {
        val cacheFile = File(context.cacheDir, fileName)
        if (!cacheFile.exists()) {
            context.assets.open(fileName).use { inputStream ->
                FileOutputStream(cacheFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        return cacheFile
    }

    // Bild laden und einmalig analysieren
    fun prepareImage(bitmap: Bitmap, onReady: () -> Unit) {
        if (encoderSession == null) return

        CoroutineScope(Dispatchers.Default).launch { //Im eigenen Thread
            try {
                println("SAM 2: Starte Bild-Analyse im Hintergrund...")
                val targetSize = 1024
                currentResizedBitmap =
                    Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)

                val inputBuffer = convertBitmapToFloatBuffer(currentResizedBitmap!!, targetSize)
                val inputShape = longArrayOf(1, 3, targetSize.toLong(), targetSize.toLong())
                val inputName = encoderSession?.inputNames?.iterator()?.next() ?: "image"

                closeOldFeatures()

                val encoderOutputs = encoderSession?.run(
                    mapOf(
                        inputName to OnnxTensor.createTensor(
                            ortEnv,
                            inputBuffer,
                            inputShape
                        )
                    )
                )

                extractAndSaveFeatures(encoderOutputs)

                println("SAM 2: Encoder fertig! Bildmerkmale im Speicher gesichert. ")
                withContext(Dispatchers.Main) { onReady() }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun convertBitmapToFloatBuffer(bitmap: Bitmap, targetSize: Int): FloatBuffer {
        val floatArray = FloatArray(3 * targetSize * targetSize)
        val pixels = IntArray(targetSize * targetSize)
        bitmap.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            floatArray[i] = (pixel shr 16 and 0xFF) / 255.0f
            floatArray[targetSize * targetSize + i] = (pixel shr 8 and 0xFF) / 255.0f
            floatArray[2 * targetSize * targetSize + i] = (pixel and 0xFF) / 255.0f
        }
        return FloatBuffer.wrap(floatArray)
    }

    private fun closeOldFeatures() {
        imageEmbed?.close()
        highResFeats0?.close()
        highResFeats1?.close()
    }

    private fun extractAndSaveFeatures(encoderOutputs: OrtSession.Result?) {
        if (encoderOutputs != null) {
            for (output in encoderOutputs) {
                val tensor = output.value as OnnxTensor
                val shape = tensor.info.shape
                if (shape.size == 4) {
                    when (shape[1].toInt()) {
                        256 -> imageEmbed = tensor
                        32 -> highResFeats0 = tensor
                        64 -> highResFeats1 = tensor
                    }
                }
            }
        }
    }

    //Bei jedem Tipp Maske berechnen
    fun segmentHold(pointsList: List<TapPoint>, onResult: (Bitmap?) -> Unit) {
        val embed = imageEmbed ?: return
        val feat0 = highResFeats0 ?: return
        val feat1 = highResFeats1 ?: return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numPoints = pointsList.size.toLong()

                val coordsTensor = createCoordsTensor(pointsList, numPoints)
                val labelsTensor = createLabelsTensor(pointsList, numPoints)

                val origSizeTensor = OnnxTensor.createTensor(
                    ortEnv,
                    IntBuffer.wrap(intArrayOf(1024, 1024)),
                    longArrayOf(2)
                )
                val hasMaskTensor = OnnxTensor.createTensor(
                    ortEnv,
                    FloatBuffer.wrap(floatArrayOf(0f)),
                    longArrayOf(1)
                )
                val dummyMask = OnnxTensor.createTensor(
                    ortEnv,
                    FloatBuffer.wrap(FloatArray(1 * 1 * 256 * 256)),
                    longArrayOf(1, 1, 256, 256)
                )

                val decoderInputs = mapOf(
                    "image_embed" to embed,
                    "high_res_feats_0" to feat0,
                    "high_res_feats_1" to feat1,
                    "point_coords" to coordsTensor,
                    "point_labels" to labelsTensor,
                    "mask_input" to dummyMask,
                    "has_mask_input" to hasMaskTensor,
                    "orig_im_size" to origSizeTensor
                )

                decoderSession?.run(decoderInputs).use { decoderOutputs ->
                    val masksTensor = decoderOutputs?.get(0) as OnnxTensor

                    // Tensor auslesen, Pixel zeichnen & Maske prüfen
                    val resultMask = processMaskTensor(masksTensor)

                    withContext(Dispatchers.Main) {
                        onResult(resultMask)
                    }
                }
                closeTensors(coordsTensor, labelsTensor, origSizeTensor, hasMaskTensor, dummyMask)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createCoordsTensor(pointsList: List<TapPoint>, numPoints: Long): OnnxTensor {
        val pointsArray = FloatArray(pointsList.size * 2)
        for (i in pointsList.indices) {
            pointsArray[i * 2] = pointsList[i].normX * 1024f
            pointsArray[i * 2 + 1] = pointsList[i].normY * 1024f
        }
        return OnnxTensor.createTensor(
            ortEnv,
            FloatBuffer.wrap(pointsArray),
            longArrayOf(1, numPoints, 2)
        )
    }

    private fun createLabelsTensor(pointsList: List<TapPoint>, numPoints: Long): OnnxTensor {
        val labelsArray = FloatArray(pointsList.size)
        for (i in pointsList.indices) {
            labelsArray[i] = if (pointsList[i].isPositive) 1f else 0f
        }
        return OnnxTensor.createTensor(
            ortEnv,
            FloatBuffer.wrap(labelsArray),
            longArrayOf(1, numPoints)
        )
    }

    private fun processMaskTensor(masksTensor: OnnxTensor): Bitmap? {
        val masksFloats = masksTensor.floatBuffer.array()
        val maskShape = masksTensor.info.shape
        val maskHeight = maskShape[maskShape.size - 2].toInt()
        val maskWidth = maskShape[maskShape.size - 1].toInt()
        val totalPixels = maskWidth * maskHeight

        val pixelsArray = IntArray(totalPixels)
        var coloredPixels = 0
        val maskColor = Color.WHITE

        for (i in 0 until totalPixels) {
            if (i < masksFloats.size && masksFloats[i] > 0.0f) {
                pixelsArray[i] = maskColor
                coloredPixels++
            } else {
                pixelsArray[i] = Color.TRANSPARENT
            }
        }


        // Maske verwerfen
        if (coloredPixels > totalPixels * 0.3f) {
            println("Maske verworfen! ($coloredPixels Pixel ist zu groß, wahrscheinlich Wand)")
            return null
        }
        val maskBitmap =
            Bitmap.createBitmap(pixelsArray, maskWidth, maskHeight, Bitmap.Config.ARGB_8888)


        // Maske auf Originalgröße hochskalieren
        val scaledMask = Bitmap.createScaledBitmap(maskBitmap, 1024, 1024, true)
        if (scaledMask !== maskBitmap) {
            maskBitmap.recycle()
        }
        val finalCroppedBitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalCroppedBitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)

        paint.maskFilter = android.graphics.BlurMaskFilter(2f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        canvas.drawBitmap(scaledMask, 0f, 0f, paint)

        paint.maskFilter = null
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        currentResizedBitmap?.let { originalFoto ->
            canvas.drawBitmap(originalFoto, 0f, 0f, paint)
        }
        val alphaMask = scaledMask.extractAlpha()
        val outlinePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            maskFilter =
                android.graphics.BlurMaskFilter(4f, android.graphics.BlurMaskFilter.Blur.SOLID)
            xfermode =
                android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OVER)
        }
        canvas.drawBitmap(alphaMask, 0f, 0f, outlinePaint)

        scaledMask.recycle()
        alphaMask.recycle()

        return finalCroppedBitmap
    }

    private fun closeTensors(vararg tensors: OnnxTensor) {
        for (tensor in tensors) {
            tensor.close()
        }
    }


}