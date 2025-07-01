package com.weatherwidget.app.data.api

import com.weatherwidget.app.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") includeAqi: String = "no"
    ): Response<WeatherResponse>
    
    @GET("forecast.json")
    suspend fun getWeatherForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7,
        @Query("aqi") includeAqi: String = "no",
        @Query("alerts") includeAlerts: String = "no"
    ): Response<WeatherResponse>
    
    companion object {
        const val BASE_URL = "https://api.weatherapi.com/v1/"
        
        // You can get a free API key from https://www.weatherapi.com/
        // For demo purposes, using a placeholder - replace with actual key
        const val API_KEY = "your_api_key_here"
    }
}