package com.example.weatherapp.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

interface RequestApi {
    @GET("forecast.json")
    suspend fun getWeather(
        @Query("key") key: String,
        @Query("q") q: String,
        @Query("days") days: String,
        @Query("aqi") aqi: String,
        @Query("alerts") alerts: String
    ): WeatherResponse
}