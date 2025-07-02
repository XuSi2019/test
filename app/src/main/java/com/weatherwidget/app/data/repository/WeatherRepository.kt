package com.weatherwidget.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.weatherwidget.app.data.api.WeatherApiService
import com.weatherwidget.app.data.api.MockWeatherData
import com.weatherwidget.app.data.model.LocationData
import com.weatherwidget.app.data.model.WeatherResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_preferences")

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    private val dataStore = context.dataStore
    
    companion object {
        private val CACHED_WEATHER_KEY = stringPreferencesKey("cached_weather")
        private val LAST_LOCATION_KEY = stringPreferencesKey("last_location")
        private val TEMPERATURE_UNIT_KEY = stringPreferencesKey("temperature_unit")
        private val UPDATE_INTERVAL_KEY = stringPreferencesKey("update_interval")
        
        const val CELSIUS = "celsius"
        const val FAHRENHEIT = "fahrenheit"
    }
    
    suspend fun getWeatherForecast(location: String): Result<WeatherResponse> {
        return try {
            // Check if API key is set to placeholder value
            if (WeatherApiService.API_KEY == "your_api_key_here") {
                // Use mock data for demo
                val mockData = MockWeatherData.getMockWeatherResponse(
                    location.split(",").firstOrNull() ?: "Demo Location"
                )
                cacheWeatherData(mockData)
                Result.success(mockData)
            } else {
                val response = apiService.getWeatherForecast(
                    apiKey = WeatherApiService.API_KEY,
                    location = location,
                    days = 7
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        // Cache the weather data
                        cacheWeatherData(weatherData)
                        Result.success(weatherData)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            // Try to return cached data if available
            getCachedWeatherData()?.let { cachedData ->
                Result.success(cachedData)
            } ?: Result.failure(e)
        }
    }
    
    suspend fun getCurrentWeather(location: String): Result<WeatherResponse> {
        return try {
            val response = apiService.getCurrentWeather(
                apiKey = WeatherApiService.API_KEY,
                location = location
            )
            
            if (response.isSuccessful) {
                response.body()?.let { weatherData ->
                    Result.success(weatherData)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun cacheWeatherData(weatherData: WeatherResponse) {
        dataStore.edit { preferences ->
            preferences[CACHED_WEATHER_KEY] = gson.toJson(weatherData)
        }
    }
    
    private suspend fun getCachedWeatherData(): WeatherResponse? {
        val cachedJson = dataStore.data.first()[CACHED_WEATHER_KEY]
        return cachedJson?.let {
            try {
                gson.fromJson(it, WeatherResponse::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun saveLastLocation(locationData: LocationData) {
        dataStore.edit { preferences ->
            preferences[LAST_LOCATION_KEY] = gson.toJson(locationData)
        }
    }
    
    suspend fun getLastLocation(): LocationData? {
        val locationJson = dataStore.data.first()[LAST_LOCATION_KEY]
        return locationJson?.let {
            try {
                gson.fromJson(it, LocationData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun getTemperatureUnit(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[TEMPERATURE_UNIT_KEY] ?: CELSIUS
        }
    }
    
    suspend fun setTemperatureUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT_KEY] = unit
        }
    }
    
    fun getUpdateInterval(): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[UPDATE_INTERVAL_KEY]?.toLongOrNull() ?: 30L // Default 30 minutes
        }
    }
    
    suspend fun setUpdateInterval(intervalMinutes: Long) {
        dataStore.edit { preferences ->
            preferences[UPDATE_INTERVAL_KEY] = intervalMinutes.toString()
        }
    }
}