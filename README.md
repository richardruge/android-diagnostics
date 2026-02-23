# Android Diagnostics

A comprehensive diagnostic application for Android devices that displays detailed system information including:

- **Device Information**: Manufacturer, model, Android version, hardware details
- **Battery Information**: Battery level, charging status, temperature, voltage
- **Memory Information**: Total, available, and used RAM
- **Storage Information**: Internal storage details
- **Network Information**: Connection status, network type, WiFi details
- **Display Information**: Screen dimensions, density, DPI
- **CPU Information**: CPU architecture and number of cores

## Building the Application

### Prerequisites
- Android Studio (Arctic Fox or later)
- Android SDK API 34
- JDK 8 or higher
- Gradle 8.1.0 or higher

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/richardruge/android-diagnostics.git
   cd android-diagnostics
   ```

2. Build the application using Gradle:
   ```bash
   ./gradlew build
   ```

3. Install on a connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```

### Running Tests

```bash
./gradlew test
```

## Features

- Real-time system diagnostics
- Clean and readable interface
- Lightweight and fast
- No special permissions required for most features
- Supports Android 7.0 (API 24) and above

## Permissions

The app requires the following permissions:
- `ACCESS_NETWORK_STATE`: To read network connection status
- `ACCESS_WIFI_STATE`: To read WiFi information
- `INTERNET`: For potential future network diagnostics

## License

This project is open source and available under the MIT License.