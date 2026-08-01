package com.example.sam.network.dto

data class BoulderDetailDto(
    val id: String,
    val name: String,
    val grade: String,
    val createdAt: String,
    val holds: List<HoldDto>
)