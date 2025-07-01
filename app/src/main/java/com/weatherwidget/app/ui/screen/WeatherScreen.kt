package com.weatherwidget.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.weatherwidget.app.data.model.ForecastDay
import com.weatherwidget.app.data.model.WeatherResponse
import com.weatherwidget.app.data.repository.WeatherRepository
import com.weatherwidget.app.ui.components.WeatherIcons
import com.weatherwidget.app.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val temperatureUnit by viewModel.temperatureUnit.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    
    // Location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )
    
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.getCurrentLocationAndWeather()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1976D2),
                        Color(0xFF42A5F5),
                        Color(0xFF90CAF9)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeatherHeader(
                    currentLocation = currentLocation,
                    onLocationClick = {
                        if (locationPermissionsState.allPermissionsGranted) {
                            viewModel.getCurrentLocationAndWeather()
                        } else {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    onSearchClick = { showSearchBar = !showSearchBar },
                    onRefreshClick = { viewModel.refreshWeatherData() },
                    onToggleUnit = { viewModel.toggleTemperatureUnit() },
                    temperatureUnit = temperatureUnit
                )
            }
            
            if (showSearchBar) {
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.searchLocationAndWeather(searchQuery)
                                showSearchBar = false
                                searchQuery = ""
                            }
                        },
                        onDismiss = { showSearchBar = false }
                    )
                }
            }
            
            when {
                                 uiState.isLoading -> {
                     item {
                         com.weatherwidget.app.ui.components.LoadingIndicator()
                     }
                 }
                 
                 uiState.error != null -> {
                     item {
                         com.weatherwidget.app.ui.components.ErrorCard(
                             error = uiState.error,
                             onRetry = { viewModel.refreshWeatherData() },
                             onDismiss = { viewModel.clearError() }
                         )
                     }
                 }
                
                                 uiState.weatherData != null -> {
                     item {
                         com.weatherwidget.app.ui.components.CurrentWeatherCard(
                             weatherData = uiState.weatherData,
                             temperatureUnit = temperatureUnit,
                             viewModel = viewModel
                         )
                     }
                     
                     item {
                         com.weatherwidget.app.ui.components.WeatherDetailsCard(
                             weatherData = uiState.weatherData
                         )
                     }
                     
                     item {
                         com.weatherwidget.app.ui.components.ForecastCard(
                             forecastDays = uiState.weatherData.forecast.forecastDays,
                             temperatureUnit = temperatureUnit,
                             viewModel = viewModel
                         )
                     }
                 }
            }
        }
    }
}

@Composable
private fun WeatherHeader(
    currentLocation: com.weatherwidget.app.data.model.LocationData?,
    onLocationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onToggleUnit: () -> Unit,
    temperatureUnit: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentLocation?.cityName ?: "Weather Widget",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (currentLocation != null) {
                    Text(
                        text = if (currentLocation.isCurrentLocation) "Current Location" else "Selected Location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onToggleUnit) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Toggle Temperature Unit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onLocationClick) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Current Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onRefreshClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search for a city...") },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = onSearch,
                enabled = query.isNotBlank()
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}