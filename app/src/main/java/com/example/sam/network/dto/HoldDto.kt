package com.example.sam.network.dto

data class HoldDto(
    val xOffset: Int,
    val yOffset: Int,
    val imageBlob: String,
    val holdType: String = "NORMAL"
)