package com.example.sam.data.model

import android.graphics.Bitmap
import java.util.UUID

data class TapPoint(
    val normX: Float,
    val normY: Float,
    val isPositive: Boolean
)

data class ClimbingHold(
    val id: String = UUID.randomUUID().toString(),
    val points: List<TapPoint> = emptyList(),
    val maskBitmap: Bitmap? = null
)

