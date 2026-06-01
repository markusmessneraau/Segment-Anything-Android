package com.example.sam.data.repository

import android.graphics.Bitmap
import com.example.sam.data.analyzer.SamLocalAnalyzer

class SamRepository(private val samAnalyzer: SamLocalAnalyzer) {

    fun loadAndPrepare(bitmap: Bitmap, onReady: () -> Unit) {
        samAnalyzer.prepareImage(bitmap, onReady)
    }

    fun getMaskAtPoint(normX: Float, normY: Float, onResult: (Bitmap) -> Unit) {
        samAnalyzer.segmentAtPoint(normX, normY, onResult)
    }
}