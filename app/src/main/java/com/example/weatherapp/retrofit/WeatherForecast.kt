package com.example.weatherapp.retrofit

data class WeatherForecast (
    val forecastDay: List<WeatherForecastDay>,
)

data class WeatherForecastDay(
    val date: String,
    val day: WeatherDay,
    val hour: List<WeatherHour>
)

data class WeatherDay(
    val maxtemp_c: String,
    val mintemp_c: String,
    val condition: WeatherCondition
)

data class WeatherHour(
    val time: String,
    val temp_c: Float,
    val condition: WeatherCondition
)