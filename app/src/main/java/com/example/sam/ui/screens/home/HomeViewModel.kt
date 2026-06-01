package com.example.sam.ui.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.example.sam.data.repository.SamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val samRepository: SamRepository) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _resultBitmap = MutableStateFlow<Bitmap?>(null)
    val resultBitmap: StateFlow<Bitmap?> = _resultBitmap.asStateFlow()

    private val _isImageReady = MutableStateFlow(false)
    val isImageReady: StateFlow<Boolean> = _isImageReady.asStateFlow()

    fun onImageSelected(context: Context, uri: Uri?) {
        _selectedImageUri.value = uri
        _resultBitmap.value = null
        _isImageReady.value = false

        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                // Bild einlesen und Merkmale berechnen
                samRepository.loadAndPrepare(argbBitmap) {
                    _isImageReady.value = true
                    _resultBitmap.value = argbBitmap
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Wird aufgerufen, wenn der Nutzer auf das Bild tippt
    fun onTrackTapped(normX: Float, normY: Float) {
        if (!_isImageReady.value) return

        samRepository.getMaskAtPoint(normX, normY) { processedBitmap ->
            _resultBitmap.value = processedBitmap
        }
    }
}