package com.weatherwidget.app.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherResponse(
    @SerializedName("current")
    val current: CurrentWeather,
    @SerializedName("forecast")
    val forecast: Forecast,
    @SerializedName("location")
    val location: Location
) : Parcelable

@Parcelize
data class CurrentWeather(
    @SerializedName("temp_c")
    val temperatureCelsius: Double,
    @SerializedName("temp_f")
    val temperatureFahrenheit: Double,
    @SerializedName("condition")
    val condition: WeatherCondition,
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("wind_kph")
    val windSpeed: Double,
    @SerializedName("wind_dir")
    val windDirection: String,
    @SerializedName("pressure_mb")
    val pressure: Double,
    @SerializedName("feelslike_c")
    val feelsLikeCelsius: Double,
    @SerializedName("feelslike_f")
    val feelsLikeFahrenheit: Double,
    @SerializedName("uv")
    val uvIndex: Double,
    @SerializedName("vis_km")
    val visibility: Double
) : Parcelable

@Parcelize
data class WeatherCondition(
    @SerializedName("text")
    val text: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("code")
    val code: Int
) : Parcelable

@Parcelize
data class Location(
    @SerializedName("name")
    val name: String,
    @SerializedName("region")
    val region: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("localtime")
    val localTime: String
) : Parcelable

@Parcelize
data class Forecast(
    @SerializedName("forecastday")
    val forecastDays: List<ForecastDay>
) : Parcelable

@Parcelize
data class ForecastDay(
    @SerializedName("date")
    val date: String,
    @SerializedName("day")
    val day: DayWeather,
    @SerializedName("hour")
    val hourlyWeather: List<HourlyWeather>
) : Parcelable

@Parcelize
data class DayWeather(
    @SerializedName("maxtemp_c")
    val maxTempCelsius: Double,
    @SerializedName("maxtemp_f")
    val maxTempFahrenheit: Double,
    @SerializedName("mintemp_c")
    val minTempCelsius: Double,
    @SerializedName("mintemp_f")
    val minTempFahrenheit: Double,
    @SerializedName("condition")
    val condition: WeatherCondition,
    @SerializedName("daily_chance_of_rain")
    val chanceOfRain: Int,
    @SerializedName("daily_chance_of_snow")
    val chanceOfSnow: Int
) : Parcelable

@Parcelize
data class HourlyWeather(
    @SerializedName("time")
    val time: String,
    @SerializedName("temp_c")
    val temperatureCelsius: Double,
    @SerializedName("temp_f")
    val temperatureFahrenheit: Double,
    @SerializedName("condition")
    val condition: WeatherCondition,
    @SerializedName("chance_of_rain")
    val chanceOfRain: Int,
    @SerializedName("chance_of_snow")
    val chanceOfSnow: Int
) : Parcelable

// UI Data Classes
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val error: String? = null,
    val lastUpdated: Long = 0L
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "",
    val isCurrentLocation: Boolean = false
)