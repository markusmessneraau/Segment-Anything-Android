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
import com.example.sam.data.model.ClimbingHold
import com.example.sam.data.model.TapPoint


class HomeViewModel(private val samRepository: SamRepository) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isImageReady = MutableStateFlow(false)
    val isImageReady: StateFlow<Boolean> = _isImageReady.asStateFlow()

    // speichert alle Griffe einer Spraywall
    private val _holds = MutableStateFlow<List<ClimbingHold>>(emptyList())
    val holds: StateFlow<List<ClimbingHold>> = _holds.asStateFlow()
    private val _activeHoldId = MutableStateFlow<String?>(null)
    val activeHoldId: StateFlow<String?> = _activeHoldId.asStateFlow()

    private val _baseBitmap = MutableStateFlow<Bitmap?>(null)
    val baseBitmap: StateFlow<Bitmap?> = _baseBitmap.asStateFlow()

    fun onImageSelected(context: Context, uri: Uri?) {
        _selectedImageUri.value = uri
        _isImageReady.value = false
        _holds.value = emptyList()
        _activeHoldId.value = null
        _baseBitmap.value = null

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
                    _baseBitmap.value = argbBitmap
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Wird aufgerufen, wenn der Nutzer auf das Bild tippt
    fun onTrackTapped(normX: Float, normY: Float) {
        if (!_isImageReady.value) return

        // Werte auf Maskengröße (1024x1024) hochrechnen
        val pixelX = (normX * 1024f).toInt().coerceIn(0, 1023)
        val pixelY = (normY * 1024f).toInt().coerceIn(0, 1023)

        // in allen fertigen Masken suchen, ob der angeklickte Pixel nicht durchsichtig ist
        val clickedHold = _holds.value.find { hold ->
            val bitmap = hold.maskBitmap
            if (bitmap != null) {
                val pixelColor = bitmap.getPixel(pixelX, pixelY)
                android.graphics.Color.alpha(pixelColor) > 0
            } else {
                false
            }
        }

        // bereits geklickter Griff neu angeklickt
        if (clickedHold != null) {
            _activeHoldId.value = clickedHold.id
        } else {
            val newPoint = TapPoint(normX, normY, isPositive = true)
            val newHold = ClimbingHold(points = listOf(newPoint))

            _activeHoldId.value = newHold.id
            _holds.value += newHold

            samRepository.getHoldMask(newHold.points) { maskBitmap ->
                val updatedHold = newHold.copy(maskBitmap = maskBitmap)
                _holds.value = _holds.value.map { hold ->
                    if (hold.id == updatedHold.id) updatedHold else hold
                }
            }

        }
    }

    fun onTrackLongPressed(normX: Float, normY: Float) {
        if (!_isImageReady.value) return

        // aktiven Griff prüfen
        val activeId = _activeHoldId.value ?: return
        val currentHold = _holds.value.find { it.id == activeId } ?: return

        val negativePoint = TapPoint(normX, normY, isPositive = false)

        val updatedPoints = currentHold.points + negativePoint
        val holdWithNewPoint = currentHold.copy(points = updatedPoints)

        _holds.value = _holds.value.map { if (it.id == activeId) holdWithNewPoint else it }

        samRepository.getHoldMask(holdWithNewPoint.points) { newMask ->
            val finishedHold = holdWithNewPoint.copy(maskBitmap = newMask)
            _holds.value = _holds.value.map { if (it.id == activeId) finishedHold else it }
        }
    }
}