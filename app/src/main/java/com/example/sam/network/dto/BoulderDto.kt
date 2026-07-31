package com.example.sam.network.dto

data class BoulderDto(
    val name: String,
    val grade: String,
    val holds: List<HoldDto>
)