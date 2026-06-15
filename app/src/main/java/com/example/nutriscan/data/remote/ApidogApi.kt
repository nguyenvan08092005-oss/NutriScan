package com.example.nutriscan.data.remote

import com.example.nutriscan.data.remote.dto.FoodResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApiService {

    @GET("searchFood")
    suspend fun searchFood(
        @Query("q") query: String
    ): FoodResponse
}