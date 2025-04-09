# NetBar

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

NetBar is an Android application that displays real-time network traffic information in your
device's status bar. It's a lightweight, efficient tool for monitoring your upload and download
bandwidth usage.

## Features

- **Real-time monitoring** of network upload and download speeds
- **Automatic unit selection** (B/s, KB/s, MB/s, GB/s)
- **Customizable display options** - choose to show upload, download, or both
- **Modern user interface** with dark theme
- **Low resource consumption** - designed to run efficiently in the background

## Screenshots

*[Screenshots would be placed here]*

## Requirements

- Android 5.0 (API level 21) or higher
- For Android 13+ users, notification permission will be requested at runtime
- For Android 14+ users, foreground service data sync permission is required

## Installation

You can install NetBar from:

1. Google Play Store [coming soon]
2. Direct APK download from the [Releases page](https://github.com/pepperonas/NetBar/releases)

## Usage

1. **Launch the app** - Open NetBar after installation
2. **Configure display options** - Choose which traffic metrics to display (upload, download, or
   both)
3. **Start monitoring** - Tap "Start Monitoring" to begin
4. **Check status bar** - Your network traffic will now be displayed in the status bar
5. **Stop when desired** - Return to the app and tap "Stop Monitoring" when finished

## Permissions

NetBar requires the following permissions:

- `FOREGROUND_SERVICE`: To run the monitoring service in the foreground
- `FOREGROUND_SERVICE_DATA_SYNC`: For Android 14+ to specify the foreground service type
- `POST_NOTIFICATIONS`: For Android 13+ to display persistent notification

## Development

### Project Structure

- `NetworkTrafficService.java` - Core service responsible for traffic monitoring
- `MainActivity.java` - Main UI for controlling the service and settings
- Layout XMLs and resource files for the user interface

### Building from Source

1. Clone the repository
   ```
   git clone https://github.com/martinpfeffer/netbar.git
   ```

2. Open the project in Android Studio

3. Build and run the application
   ```
   ./gradlew assembleDebug
   ```

## License

```
Copyright 2025 Martin Pfeffer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contact

Martin Pfeffer

- GitHub: [martinpfeffer](https://github.com/pepperonas)
- Email: [martin.pfeffer@celox.io]
- Website: [https://celox.io]
- Apps & Tools: [https://mrx3k1.de]

## Acknowledgments

- Icon designed using [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)