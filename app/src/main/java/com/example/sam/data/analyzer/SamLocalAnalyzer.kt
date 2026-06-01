package com.example.sam.data.analyzer

import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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

    // Bild laden und EINMALIG analysieren (Encoder)
    fun prepareImage(bitmap: Bitmap, onReady: () -> Unit) {
        if (encoderSession == null) return

        CoroutineScope(Dispatchers.Default).launch { //Im eigene Thread
            try {
                println("SAM 2: Starte Bild-Analyse im Hintergrund...")
                val targetSize = 1024
                currentResizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)

                val floatArray = FloatArray(3 * targetSize * targetSize)
                val pixels = IntArray(targetSize * targetSize)
                currentResizedBitmap!!.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)

                for (i in pixels.indices) {
                    val pixel = pixels[i]
                    floatArray[i] = (pixel shr 16 and 0xFF) / 255.0f
                    floatArray[targetSize * targetSize + i] = (pixel shr 8 and 0xFF) / 255.0f
                    floatArray[2 * targetSize * targetSize + i] = (pixel and 0xFF) / 255.0f
                }

                val inputBuffer = FloatBuffer.wrap(floatArray)
                val inputShape = longArrayOf(1, 3, targetSize.toLong(), targetSize.toLong())

                val inputName = encoderSession?.inputNames?.iterator()?.next() ?: "image"

                // alte merkmale schlißen, falls ein neues Bild geladen wird
                imageEmbed?.close()
                highResFeats0?.close()
                highResFeats1?.close()

                val encoderOutputs = encoderSession?.run(mapOf(inputName to OnnxTensor.createTensor(ortEnv, inputBuffer, inputShape)))

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
                    println("SAM 2: Encoder fertig! Bildmerkmale im Speicher gesichert. ")
                    withContext(Dispatchers.Main) { onReady() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //Bei jedem Fingertipp die Maske berechnen (Decoder)
    fun segmentAtPoint(normX: Float, normY: Float, onResult: (Bitmap) -> Unit) {
        val embed = imageEmbed ?: return
        val feat0 = highResFeats0 ?: return
        val feat1 = highResFeats1 ?: return
        val baseBitmap = currentResizedBitmap ?: return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Koordinaten auf die Modellgröße (1024x1024) mappen
                val points = floatArrayOf(normX * 1024f, normY * 1024f)
                val labels = floatArrayOf(1f) // 1 = Klickpunkt einschließen

                val coordsTensor = OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(points), longArrayOf(1, 1, 2))
                val labelsTensor = OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(labels), longArrayOf(1, 1))
                val origSizeTensor = OnnxTensor.createTensor(ortEnv, IntBuffer.wrap(intArrayOf(1024, 1024)), longArrayOf(2))
                val hasMaskTensor = OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(floatArrayOf(0f)), longArrayOf(1))
                val dummyMask = OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(FloatArray(1 * 1 * 256 * 256)), longArrayOf(1, 1, 256, 256))

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
                    val masksFloats = masksTensor.floatBuffer.array()

                    val maskShape = masksTensor.info.shape
                    val maskHeight = maskShape[maskShape.size - 2].toInt()
                    val maskWidth = maskShape[maskShape.size - 1].toInt()

                    val maskBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)

                    for (y in 0 until maskHeight) {
                        for (x in 0 until maskWidth) {
                            val index = y * maskWidth + x
                            if (index < masksFloats.size && masksFloats[index] > 0.0f) {
                                maskBitmap.setPixel(x, y, Color.argb(160, 0, 255, 0)) // Klick-Ergebnis GRÜN einfärben!
                            } else {
                                maskBitmap.setPixel(x, y, Color.TRANSPARENT)
                            }
                        }
                    }

                    val finalResult = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(finalResult)
                    canvas.drawBitmap(baseBitmap, 0f, 0f, null)

                    val scaledMask = Bitmap.createScaledBitmap(maskBitmap, 1024, 1024, true)
                    canvas.drawBitmap(scaledMask, 0f, 0f, null)

                    maskBitmap.recycle()
                    scaledMask.recycle()

                    withContext(Dispatchers.Main) {
                        onResult(finalResult)
                    }
                }

                coordsTensor.close()
                labelsTensor.close()
                origSizeTensor.close()
                hasMaskTensor.close()
                dummyMask.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}