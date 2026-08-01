package com.example.sam.network.dto

data class BoulderCreateDto(
    val name: String,
    val grade: String,
    val holds: List<HoldDto>
)