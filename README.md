# MaccApp Project - Hand Drawing Application

## Overview

This Android application, developed by Matthias Bardonnet and Nabil Taha Yassine, allows users to draw on the screen using hand tracking. The application leverages the power of Google's MediaPipe for real-time hand landmark detection and Firebase for data storage and user authentication.

## Features

-   **Real-time Hand Tracking:** Uses MediaPipe to detect hand landmarks in real-time using the device's camera.
-   **On-Screen Drawing:** Allows users to draw on the screen by tracking the tip of their index finger.
-   **Customizable Drawing:** Users can change the drawing color and stroke size.
-   **Save Drawings:** Drawings can be saved and stored in Firebase.
-   **User Authentication:** Implements user login and signup using Firebase Authentication.
-   **Gallery:** Users can view and delete their saved drawings.

## Technologies Used

-   **Android Studio:** The primary IDE for developing the Android application.
-   **Kotlin:** The programming language used for the application's logic and UI.
-   **Jetpack Compose:** The modern UI toolkit for building native Android UIs.
-   **MediaPipe:** Google's framework for building multimodal applied AI pipelines. Specifically, the hand landmark detection model is used.
-   **Firebase:** Google's mobile development platform used for:
    -   **Authentication:** Managing user accounts (login, signup, logout).
    -   **Firestore:** Storing and retrieving user drawings.
-   **Coil:** An image loading library for Android backed by Kotlin Coroutines.

## Project Structure

The project is structured as a standard Android Studio project with the following key components:

-   **`app/`:** Contains the main application code.
    -   **`java/com/example/maccappproject/`:** Contains the Kotlin source code.
        -   **`components/`:** Contains reusable UI components like `CameraView`, `OverlayView`, and `DrawingCanvas`.
        -   **`helpers/`:** Contains helper classes for MediaPipe integration, such as `HandLandmarkerHelper`.
        -   **`navigation/`:** Contains navigation logic using Jetpack Navigation.
        -   **`screens/`:** Contains the composable functions for each screen of the application (e.g., `HomeScreen`, `DrawingScreen`, `LoginScreen`).
        -   **`utils/`:** Contains utility classes, including `FirebaseManager` for Firebase interactions.
    -   **`res/`:** Contains resources like layouts, drawables, and values.

## Setup Instructions

1.  **Clone the Repository:**
2.  **Open in Android Studio:**
    -   Open Android Studio and select "Open an existing project."
    -   Navigate to the cloned repository and select the `build.gradle` file.
3.  **Firebase Setup:**
    -   Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    -   Add an Android app to your Firebase project.
    -   Download the `google-services.json` file and place it in the `app/` directory.
    -   Enable Firebase Authentication and Firestore in your Firebase project.
4.  **MediaPipe Setup:**
    -   The MediaPipe library is included as a dependency in the `build.gradle` file.
    -   Ensure that the necessary MediaPipe models are included in the `assets` folder.
5.  **Build and Run:**
    -   Connect an Android device or use an emulator.
    -   Click the "Run" button in Android Studio to build and run the application.

## Authors

-   Matthias Bardonnet
-   Nabil Taha Yassine

## License

This project is licensed under the [Specify License if any].

## Disclaimer

This application is a demonstration of hand tracking and drawing capabilities using MediaPipe and Firebase. It is not intended for commercial use.