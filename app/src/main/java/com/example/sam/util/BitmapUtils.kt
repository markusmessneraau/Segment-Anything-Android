package com.example.sam.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun decodeSampledBitmapFromResource(
    resources: Resources,
    resId: Int,
    maxSize: Int = 2048
): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(resources, resId, bounds)

    var inSampleSize = 1
    while (bounds.outWidth / inSampleSize > maxSize || bounds.outHeight / inSampleSize > maxSize) {
        inSampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
    return BitmapFactory.decodeResource(resources, resId, options)
}

fun cropToSquareCenter(bitmap: Bitmap): Bitmap {
    val size = minOf(bitmap.width, bitmap.height)
    val xOffset = (bitmap.width - size) / 2
    val yOffset = (bitmap.height - size) / 2
    return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
}
