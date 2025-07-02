package com.weatherwidget.app.service

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import com.weatherwidget.app.R
import com.weatherwidget.app.data.repository.WeatherRepository
import com.weatherwidget.app.location.LocationManager
import com.weatherwidget.app.widget.WeatherWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WeatherUpdateService : Service() {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    @Inject
    lateinit var locationManager: LocationManager
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateWeatherWidget()
        return START_NOT_STICKY
    }
    
    private fun updateWeatherWidget() {
        serviceScope.launch {
            try {
                // Get last known location
                val location = weatherRepository.getLastLocation()
                if (location != null) {
                    // Fetch weather data
                    val weatherResult = weatherRepository.getWeatherForecast(
                        "${location.latitude},${location.longitude}"
                    )
                    
                    weatherResult.onSuccess { weatherData ->
                        // Update all widgets
                        val appWidgetManager = AppWidgetManager.getInstance(this@WeatherUpdateService)
                        val widgetComponent = ComponentName(this@WeatherUpdateService, WeatherWidgetReceiver::class.java)
                        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
                        
                        for (widgetId in widgetIds) {
                            val views = RemoteViews(packageName, R.layout.weather_widget)
                            
                            // Update widget content
                            views.setTextViewText(R.id.widget_location, weatherData.location.name)
                            views.setTextViewText(
                                R.id.widget_temperature,
                                "${weatherData.current.temperatureCelsius.toInt()}°C"
                            )
                            views.setTextViewText(R.id.widget_condition, weatherData.current.condition.text)
                            
                            // Update weather icon based on condition
                            val iconResource = when (weatherData.current.condition.code) {
                                1000 -> R.drawable.ic_weather_sunny
                                else -> R.drawable.ic_weather_sunny
                            }
                            views.setImageViewResource(R.id.widget_icon, iconResource)
                            
                            appWidgetManager.updateAppWidget(widgetId, views)
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error silently for background service
            } finally {
                stopSelf()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}