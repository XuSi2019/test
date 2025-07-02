package com.weatherwidget.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwidget.app.data.model.LocationData
import com.weatherwidget.app.data.model.WeatherUiState
import com.weatherwidget.app.data.repository.WeatherRepository
import com.weatherwidget.app.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationManager: LocationManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    private val _temperatureUnit = MutableStateFlow(WeatherRepository.CELSIUS)
    val temperatureUnit: StateFlow<String> = _temperatureUnit.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            // Load temperature unit preference
            weatherRepository.getTemperatureUnit().first().let { unit ->
                _temperatureUnit.value = unit
            }
            
            // Try to load last known location and weather data
            weatherRepository.getLastLocation()?.let { location ->
                _currentLocation.value = location
                loadWeatherData("${location.latitude},${location.longitude}")
            } ?: getCurrentLocationAndWeather()
        }
    }
    
    fun getCurrentLocationAndWeather() {
        if (!locationManager.hasLocationPermission()) {
            _uiState.value = _uiState.value.copy(
                error = "Location permission is required to get current weather"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            locationManager.getCurrentLocation()
                .onSuccess { locationData ->
                    _currentLocation.value = locationData
                    weatherRepository.saveLastLocation(locationData)
                    loadWeatherData("${locationData.latitude},${locationData.longitude}")
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to get location: ${error.message}"
                    )
                }
        }
    }
    
    fun searchLocationAndWeather(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Try to get location from geocoding first
            locationManager.getLocationFromQuery(query)
                .onSuccess { locationData ->
                    _currentLocation.value = locationData
                    weatherRepository.saveLastLocation(locationData)
                    loadWeatherData("${locationData.latitude},${locationData.longitude}")
                }
                .onFailure {
                    // If geocoding fails, try using the query directly with the API
                    loadWeatherData(query)
                }
        }
    }
    
    private fun loadWeatherData(location: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            weatherRepository.getWeatherForecast(location)
                .onSuccess { weatherResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        weatherData = weatherResponse,
                        error = null,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load weather data: ${error.message}"
                    )
                }
        }
    }
    
    fun refreshWeatherData() {
        _currentLocation.value?.let { location ->
            loadWeatherData("${location.latitude},${location.longitude}")
        } ?: getCurrentLocationAndWeather()
    }
    
    fun toggleTemperatureUnit() {
        viewModelScope.launch {
            val newUnit = if (_temperatureUnit.value == WeatherRepository.CELSIUS) {
                WeatherRepository.FAHRENHEIT
            } else {
                WeatherRepository.CELSIUS
            }
            _temperatureUnit.value = newUnit
            weatherRepository.setTemperatureUnit(newUnit)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getTemperatureDisplay(tempC: Double, tempF: Double): String {
        return if (_temperatureUnit.value == WeatherRepository.CELSIUS) {
            "${tempC.toInt()}°C"
        } else {
            "${tempF.toInt()}°F"
        }
    }
    
    fun getTemperatureValue(tempC: Double, tempF: Double): Double {
        return if (_temperatureUnit.value == WeatherRepository.CELSIUS) tempC else tempF
    }
}