package com.weatherwidget.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.weatherwidget.app.data.model.LocationData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getCurrentLocation(): Result<LocationData> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }
        
        return try {
            val location = getCurrentLocationInternal()
            val cityName = getCityNameFromCoordinates(location.latitude, location.longitude)
            
            Result.success(
                LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = cityName,
                    isCurrentLocation = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    @Suppress("MissingPermission")
    private suspend fun getCurrentLocationInternal(): Location = suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()
        
        continuation.invokeOnCancellation {
            cancellationTokenSource.cancel()
        }
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                continuation.resume(location)
            } else {
                continuation.resume(getLastKnownLocationSync())
            }
        }.addOnFailureListener { exception ->
            // Fallback to last known location
            try {
                continuation.resume(getLastKnownLocationSync())
            } catch (e: Exception) {
                continuation.resume(getDefaultLocation())
            }
        }
    }
    
    @Suppress("MissingPermission")
    private fun getLastKnownLocationSync(): Location {
        return try {
            fusedLocationClient.lastLocation.result ?: getDefaultLocation()
        } catch (e: Exception) {
            getDefaultLocation()
        }
    }
    
    private fun getDefaultLocation(): Location {
        // Default to New York City coordinates
        return Location("default").apply {
            latitude = 40.7128
            longitude = -74.0060
        }
    }
    
    private suspend fun getCityNameFromCoordinates(latitude: Double, longitude: Double): String {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.locality ?: "Unknown Location"
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.locality ?: "Unknown Location"
            }
        } catch (e: Exception) {
            "Unknown Location"
        }
    }
    
    suspend fun getLocationFromQuery(query: String): Result<LocationData> {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocationName(query, 1)
                val address = addresses?.firstOrNull()
                    ?: return Result.failure(Exception("Location not found"))
                
                Result.success(
                    LocationData(
                        latitude = address.latitude,
                        longitude = address.longitude,
                        cityName = address.locality ?: query,
                        isCurrentLocation = false
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                val address = addresses?.firstOrNull()
                    ?: return Result.failure(Exception("Location not found"))
                
                Result.success(
                    LocationData(
                        latitude = address.latitude,
                        longitude = address.longitude,
                        cityName = address.locality ?: query,
                        isCurrentLocation = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}