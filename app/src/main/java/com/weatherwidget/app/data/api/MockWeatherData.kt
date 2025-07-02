package com.weatherwidget.app.data.api

import com.weatherwidget.app.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object MockWeatherData {
    
    fun getMockWeatherResponse(location: String = "New York"): WeatherResponse {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        return WeatherResponse(
            current = CurrentWeather(
                temperatureCelsius = 22.0,
                temperatureFahrenheit = 72.0,
                condition = WeatherCondition(
                    text = "Partly Cloudy",
                    icon = "//cdn.weatherapi.com/weather/64x64/day/116.png",
                    code = 1003
                ),
                humidity = 65,
                windSpeed = 15.0,
                windDirection = "SW",
                pressure = 1013.0,
                feelsLikeCelsius = 25.0,
                feelsLikeFahrenheit = 77.0,
                uvIndex = 6.0,
                visibility = 10.0
            ),
            forecast = Forecast(
                forecastDays = generateMockForecast()
            ),
            location = Location(
                name = location,
                region = "NY",
                country = "United States",
                latitude = 40.7128,
                longitude = -74.0060,
                localTime = currentTime
            )
        )
    }
    
    private fun generateMockForecast(): List<ForecastDay> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val forecastDays = mutableListOf<ForecastDay>()
        
        val conditions = listOf(
            WeatherCondition("Sunny", "//cdn.weatherapi.com/weather/64x64/day/113.png", 1000),
            WeatherCondition("Partly Cloudy", "//cdn.weatherapi.com/weather/64x64/day/116.png", 1003),
            WeatherCondition("Cloudy", "//cdn.weatherapi.com/weather/64x64/day/119.png", 1006),
            WeatherCondition("Light Rain", "//cdn.weatherapi.com/weather/64x64/day/296.png", 1183),
            WeatherCondition("Thunderstorm", "//cdn.weatherapi.com/weather/64x64/day/389.png", 1087)
        )
        
        val baseTemp = 20.0
        
        for (i in 0..6) {
            val date = dateFormat.format(calendar.time)
            val tempVariation = (Math.random() * 10) - 5 // Random variation of ±5°C
            val maxTemp = baseTemp + tempVariation + (Math.random() * 5)
            val minTemp = maxTemp - (5 + Math.random() * 10)
            
            val condition = conditions[(Math.random() * conditions.size).toInt()]
            
            forecastDays.add(
                ForecastDay(
                    date = date,
                    day = DayWeather(
                        maxTempCelsius = maxTemp,
                        maxTempFahrenheit = (maxTemp * 9/5) + 32,
                        minTempCelsius = minTemp,
                        minTempFahrenheit = (minTemp * 9/5) + 32,
                        condition = condition,
                        chanceOfRain = (Math.random() * 100).toInt(),
                        chanceOfSnow = if (maxTemp < 5) (Math.random() * 30).toInt() else 0
                    ),
                    hourlyWeather = generateMockHourlyWeather(date, minTemp, maxTemp, condition)
                )
            )
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return forecastDays
    }
    
    private fun generateMockHourlyWeather(
        date: String,
        minTemp: Double,
        maxTemp: Double,
        dayCondition: WeatherCondition
    ): List<HourlyWeather> {
        val hourlyWeather = mutableListOf<HourlyWeather>()
        
        for (hour in 0..23) {
            val hourlyTemp = minTemp + (maxTemp - minTemp) * 
                (Math.sin((hour - 6) * Math.PI / 12) + 1) / 2
            
            hourlyWeather.add(
                HourlyWeather(
                    time = "$date ${hour.toString().padStart(2, '0')}:00",
                    temperatureCelsius = hourlyTemp,
                    temperatureFahrenheit = (hourlyTemp * 9/5) + 32,
                    condition = dayCondition,
                    chanceOfRain = (Math.random() * 100).toInt(),
                    chanceOfSnow = if (hourlyTemp < 5) (Math.random() * 30).toInt() else 0
                )
            )
        }
        
        return hourlyWeather
    }
}