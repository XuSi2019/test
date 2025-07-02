# Weather Widget for Android

A beautiful and feature-rich weather widget application for Android built with modern Android development practices including Jetpack Compose, Material 3 Design, and MVVM architecture.

## Features

### 🌟 Main Features
- **Real-time Weather Data**: Current weather conditions with detailed information
- **7-Day Forecast**: Extended weather forecast with daily highs and lows
- **Home Screen Widget**: Beautiful widget that can be added to Android home screen
- **Location Services**: GPS-based current location or manual city search
- **Temperature Units**: Switch between Celsius and Fahrenheit
- **Beautiful UI**: Modern Material 3 design with smooth animations
- **Offline Support**: Cached weather data for offline viewing
- **Dark Theme**: Automatic dark/light theme support

### 📱 Technical Features
- **Jetpack Compose UI**: Modern declarative UI framework
- **Material 3 Design**: Latest Material Design components and theming
- **MVVM Architecture**: Clean architecture with ViewModel and Repository pattern
- **Dependency Injection**: Hilt for dependency management
- **Reactive Programming**: Kotlin Coroutines and Flow for async operations
- **Network Caching**: Smart caching with DataStore for persistence
- **Permission Handling**: Runtime location permission requests
- **Error Handling**: Comprehensive error handling with retry mechanisms

## Screenshots

The app features:
- **Main Weather Screen**: Displays current weather with location, temperature, conditions, and detailed weather information
- **7-Day Forecast**: Horizontal scrollable forecast cards
- **Search Functionality**: Search for any city worldwide
- **Home Screen Widget**: Compact widget showing essential weather info
- **Settings**: Temperature unit preferences and update intervals

## API Integration

This app uses the WeatherAPI.com service for weather data. To use the app:

1. Sign up for a free API key at [WeatherAPI.com](https://www.weatherapi.com/)
2. Replace the placeholder API key in `app/src/main/java/com/weatherwidget/app/data/api/WeatherApiService.kt`:

```kotlin
companion object {
    const val BASE_URL = "https://api.weatherapi.com/v1/"
    const val API_KEY = "your_actual_api_key_here" // Replace this
}
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 26 or higher
- Kotlin 1.9.22 or newer

### Installation
1. Clone this repository:
   ```bash
   git clone [repository-url]
   cd weather-widget-android
   ```

2. Open the project in Android Studio

3. Get your API key from WeatherAPI.com and update the `API_KEY` constant

4. Build and run the project:
   ```bash
   ./gradlew assembleDebug
   ```

### Adding the Widget
1. Install the app on your Android device
2. Long press on the home screen
3. Select "Widgets" from the menu
4. Find "Weather Widget" and drag it to your home screen
5. The widget will start showing weather data automatically

## Project Structure

```
app/
├── src/main/java/com/weatherwidget/app/
│   ├── data/
│   │   ├── api/          # API service interfaces
│   │   ├── model/        # Data models and DTOs
│   │   └── repository/   # Data repository implementation
│   ├── di/               # Dependency injection modules
│   ├── location/         # Location management
│   ├── ui/
│   │   ├── components/   # Reusable UI components
│   │   ├── screen/       # Main app screens
│   │   ├── theme/        # App theming
│   │   └── viewmodel/    # ViewModels for UI state
│   ├── widget/           # Android widget implementation
│   ├── MainActivity.kt
│   └── WeatherApplication.kt
├── src/main/res/
│   ├── drawable/         # Vector drawables and shapes
│   ├── layout/           # XML layouts for widgets
│   ├── values/           # Strings, colors, themes
│   └── xml/              # Widget metadata
└── build.gradle          # App dependencies
```

## Dependencies

### Core Android
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Latest Material Design components
- **Hilt**: Dependency injection
- **Navigation Compose**: Navigation between screens
- **DataStore**: Data persistence
- **Work Manager**: Background tasks

### Networking & Data
- **Retrofit**: HTTP client for API calls
- **Gson**: JSON parsing
- **OkHttp**: HTTP logging and networking

### Location & Permissions
- **Google Play Services Location**: GPS and location services
- **Accompanist Permissions**: Runtime permission handling

### Utilities
- **Kotlin Coroutines**: Asynchronous programming
- **Lifecycle Components**: ViewModel and LiveData
- **Coil**: Image loading for weather icons

## Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION`: For GPS-based current location
- `ACCESS_COARSE_LOCATION`: For approximate location
- `INTERNET`: For downloading weather data
- `ACCESS_NETWORK_STATE`: For checking network connectivity

## Architecture

The app follows the **MVVM (Model-View-ViewModel)** architecture pattern:

- **Model**: Data classes, API services, and repository
- **View**: Jetpack Compose UI components
- **ViewModel**: Business logic and UI state management

### Data Flow
1. **UI** triggers actions in **ViewModel**
2. **ViewModel** calls **Repository** methods
3. **Repository** fetches data from **API** or **Cache**
4. Data flows back through **ViewModel** to **UI**

## Customization

### Adding New Weather Conditions
To add support for new weather conditions:
1. Update `WeatherIcons.kt` with new condition codes
2. Add corresponding Material Icons
3. Update condition descriptions

### Theming
The app uses Material 3 dynamic theming. Colors can be customized in:
- `app/src/main/res/values/colors.xml`
- `app/src/main/java/com/weatherwidget/app/ui/theme/`

### Widget Customization
Widget appearance can be modified in:
- `app/src/main/res/layout/weather_widget.xml`
- `app/src/main/res/drawable/widget_background.xml`

## Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **WeatherAPI.com** for providing free weather data API
- **Material Design** team for beautiful design components
- **Android Jetpack** team for modern development tools

## Support

If you encounter any issues or have questions:
1. Check the [Issues](../../issues) page
2. Create a new issue with detailed information
3. Include device information and steps to reproduce

---

**Note**: This is a demo application. For production use, consider implementing additional features like:
- Weather alerts and notifications
- Multiple location support
- Historical weather data
- Weather maps integration
- Widget size variants