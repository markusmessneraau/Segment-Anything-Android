package com.example.sam.network

import com.example.sam.network.dto.BoulderCreateDto
import com.example.sam.network.dto.BoulderDetailDto
import com.example.sam.network.dto.BoulderListDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/boulders")
    suspend fun saveRoute(@Body route: BoulderCreateDto)

    @GET("api/boulders")
    suspend fun getAllBoulders(): List<BoulderListDto>

    @GET("api/boulders/{id}")
    suspend fun getBoulderById(
        @Path("id") id: String
    ): BoulderDetailDto


}