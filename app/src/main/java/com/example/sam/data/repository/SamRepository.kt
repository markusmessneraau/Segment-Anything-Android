package com.example.sam.data.repository

import android.graphics.Bitmap
import com.example.sam.data.analyzer.SamLocalAnalyzer
import com.example.sam.data.model.TapPoint

class SamRepository(private val samAnalyzer: SamLocalAnalyzer) {

    fun loadAndPrepare(bitmap: Bitmap, onReady: () -> Unit) {
        samAnalyzer.prepareImage(bitmap, onReady)
    }

    fun getHoldMask(points: List<TapPoint>, onResult: (Bitmap) -> Unit) {
        samAnalyzer.segmentHold(points, onResult)
    }
}