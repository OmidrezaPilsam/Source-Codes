# BieaWebViewer

A simple Android application that displays the website www.biea.xyz using a WebView.

## Features

- Displays the website `www.biea.xyz`.
- JavaScript is enabled in the WebView.
- External links open in the device's default browser.
- Shows a progress bar while the page is loading.
- Includes a refresh button to reload the page.
- Handles network errors and displays messages to the user.
- Provides an offline message if no internet connection is detected, with an option to retry via the refresh button.
- Basic responsive design for different screen sizes.

## Prerequisites

- Android Studio (latest stable version recommended)
- Android SDK (corresponding to the `compileSdk` and `minSdk` versions in `app/build.gradle`)
- Kotlin plugin for Android Studio

## How to Build and Run

1.  **Clone the repository or download the source code.**
    ```bash
    # If it were a git repo:
    # git clone <repository-url>
    # cd BieaWebViewer
    ```
    (For now, assume the user has the `BieaWebViewer` directory)
2.  **Open the project in Android Studio:**
    - Launch Android Studio.
    - Select "Open" or "Open an Existing Project".
    - Navigate to the `BieaWebViewer` directory and select it.
3.  **Let Android Studio sync the project with Gradle files.** This might take a few moments.
4.  **Run the application:**
    - Select an Android Virtual Device (AVD) or connect a physical Android device.
    - Click the "Run" button (green play icon) in Android Studio, or select "Run" > "Run 'app'" from the menu.

## Dependencies

The project uses standard AndroidX libraries and Kotlin:

- `androidx.core:core-ktx`
- `androidx.appcompat:appcompat`
- `com.google.android.material:material` (Though not explicitly used for major components yet, it's good practice for themes and potential future UI elements like Snackbar)
- `androidx.constraintlayout:constraintlayout`
- Kotlin Standard Library (`org.jetbrains.kotlin:kotlin-stdlib`)

These dependencies are listed in the `BieaWebViewer/app/build.gradle` file and are automatically managed by Gradle.

## Code Structure

- `app/src/main/java/com/example/bieawebviewer/MainActivity.kt`: The main activity handling the WebView and UI logic.
- `app/src/main/res/layout/activity_main.xml`: The XML layout file for the main activity.
- `app/src/main/AndroidManifest.xml`: Contains app permissions and component declarations.
- `app/build.gradle`: App-level Gradle build script, including dependencies.

## Notes

- Ensure you have an active internet connection to load the website.
- The application requests internet permission, which is necessary for its core functionality.
