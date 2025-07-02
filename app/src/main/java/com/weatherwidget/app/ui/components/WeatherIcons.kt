package com.weatherwidget.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

object WeatherIcons {
    
    fun getWeatherIcon(conditionCode: Int): ImageVector {
        return when (conditionCode) {
            1000 -> Icons.Default.WbSunny // Sunny
            1003 -> Icons.Default.CloudQueue // Partly cloudy
            1006, 1009 -> Icons.Default.WbCloudy // Cloudy, Overcast
            1030, 1135, 1147 -> Icons.Default.Cloud // Mist, Fog
            1063, 1066, 1069, 1072, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1201, 1240, 1243, 1246 -> Icons.Default.Grain // Rain
            1087, 1273, 1276, 1279, 1282 -> Icons.Default.Thunderstorm // Thunderstorm
            else -> Icons.Default.WbSunny // Default to sunny
        }
    }
    
    fun getWeatherDescription(conditionCode: Int): String {
        return when (conditionCode) {
            1000 -> "Sunny"
            1003 -> "Partly Cloudy"
            1006 -> "Cloudy"
            1009 -> "Overcast"
            1030 -> "Mist"
            1063 -> "Patchy rain possible"
            1066 -> "Patchy snow possible"
            1069 -> "Patchy sleet possible"
            1072 -> "Patchy freezing drizzle possible"
            1087 -> "Thundery outbreaks possible"
            1114 -> "Blowing snow"
            1117 -> "Blizzard"
            1135 -> "Fog"
            1147 -> "Freezing fog"
            1150 -> "Patchy light drizzle"
            1153 -> "Light drizzle"
            1168 -> "Freezing drizzle"
            1171 -> "Heavy freezing drizzle"
            1180 -> "Patchy light rain"
            1183 -> "Light rain"
            1186 -> "Moderate rain at times"
            1189 -> "Moderate rain"
            1192 -> "Heavy rain at times"
            1195 -> "Heavy rain"
            1198 -> "Light freezing rain"
            1201 -> "Moderate or heavy freezing rain"
            1204 -> "Light sleet"
            1207 -> "Moderate or heavy sleet"
            1210 -> "Patchy light snow"
            1213 -> "Light snow"
            1216 -> "Patchy moderate snow"
            1219 -> "Moderate snow"
            1222 -> "Patchy heavy snow"
            1225 -> "Heavy snow"
            1237 -> "Ice pellets"
            1240 -> "Light rain shower"
            1243 -> "Moderate or heavy rain shower"
            1246 -> "Torrential rain shower"
            1249 -> "Light sleet showers"
            1252 -> "Moderate or heavy sleet showers"
            1255 -> "Light snow showers"
            1258 -> "Moderate or heavy snow showers"
            1261 -> "Light showers of ice pellets"
            1264 -> "Moderate or heavy showers of ice pellets"
            1273 -> "Patchy light rain with thunder"
            1276 -> "Moderate or heavy rain with thunder"
            1279 -> "Patchy light snow with thunder"
            1282 -> "Moderate or heavy snow with thunder"
            else -> "Unknown"
        }
    }
}