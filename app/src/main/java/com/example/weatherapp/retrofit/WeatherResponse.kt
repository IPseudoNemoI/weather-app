package com.example.weatherapp.retrofit

data class WeatherResponse(
    val location: WeatherLocation,
    val current: WeatherCurrent,
    val forecast: WeatherForecast
)

data class WeatherLocation(
    val name: String,
    val localtime: String
)


data class WeatherCurrent(
    val last_updated: String,
    val temp_c: Float,
    val condition: WeatherCondition
)

data class WeatherCondition(
    val text: String,
    val icon: String
)
