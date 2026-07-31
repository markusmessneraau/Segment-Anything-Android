package com.example.sam.network

import com.example.sam.network.dto.BoulderDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/boulders")
    suspend fun saveRoute(@Body route: BoulderDto)
}