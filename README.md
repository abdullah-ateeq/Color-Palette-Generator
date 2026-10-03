# ChromaMix — Color Palette Generator 🎨

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![MinSDK](https://img.shields.io/badge/MinSDK-24-orange.svg)](https://developer.android.com/about/versions/nougat)
[![License](https://img.shields.io/badge/License-MIT-brightgreen.svg)](LICENSE)

**ChromaMix** is a modern, intuitive Android application designed for designers, developers, and artists to create, customize, and manage beautiful color palettes effortlessly. Built natively with **Kotlin**, **Jetpack Compose**, and **Material 3**, ChromaMix leverages color theory algorithms to instantly generate harmonious color schemes.

---

## ✨ Features

- **🎨 Color Theory Harmonies**: Instantly generate palettes using 5 foundational rules:
  - **Monochromatic**: Subtle variations in lightness and saturation.
  - **Complementary**: High-contrast opposite colors on the color wheel.
  - **Analogous**: Smooth, adjacent color harmonies.
  - **Triadic**: Vibrant, balanced three-point color distribution.
  - **Split-Complementary**: High contrast with reduced tension.
- **🖌️ Interactive Color Picker**: Fine-tune base colors via:
  - Intuitive Visual Color Picker Canvas (HSV spectrum).
  - Direct 6-digit HEX text input (`#RRGGBB`).
  - Precise RGB (0–255) sliders.
  - Precise HSL (Hue, Saturation, Lightness) controls.
- **🔒 Color Locking**: Lock specific color swatches in place while regenerating or tweaking the rest of the palette.
- **🔢 Adjustable Palette Sizes**: Generate 3-color, 5-color, or 7-color palettes.
- **📋 Seamless Clipboard Support**: Copy individual HEX codes or full palette lists with a single tap.
- **💾 Offline Palette Storage**: Save, rename, restore, and organize up to 50 custom palettes powered by AndroidX DataStore.
- **🌓 Dynamic Material You & Dark Mode**: Full support for System, Light, and Dark themes with optional Android 12+ Dynamic Coloring.
- **⚡ 100% Offline & Private**: Zero network permissions required. Your color palettes remain stored locally on your device.

---

## 📸 Screenshots & Workflow

| Generator Screen | Saved Palettes | Customization & Settings |
| :---: | :---: | :---: |
| *Interactive picker, locking & harmonies* | *Saved collection & palette management* | *Themes, defaults & HSL/RGB toggles* |

---

## 🛠️ Architecture & Tech Stack

ChromaMix follows **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** pattern with Unidirectional Data Flow (UDF).

```
com.example.chromamix
 ├── data
 │    ├── model       # Data classes (RgbColor, HslColor, Palette, AppSettings)
 │    └── repository  # PalettePreferencesRepository (DataStore & JSON serializer)
 ├── domain
 │    ├── ColorUtils        # RGB/HSL/HSV conversion, HEX validation, contrast utilities
 │    └── PaletteGenerator  # Math & algorithms for color harmony generation
 └── ui
      ├── components  # Reusable Compose components (ColorPicker, Swatch, Selectors)
      ├── navigation  # Jetpack Navigation (NavHost, BottomNavBar, Screen routes)
      ├── screens     # App screens (Generator, Saved, Settings, About, Splash)
      ├── theme       # Material 3 Color scheme, Typography, Theme wrapper
      └── viewmodel   # ChromaMixViewModel & ViewModelFactory
```

### Key Libraries & Tools
- **Language**: Kotlin 2.x
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **State Management**: Kotlin Coroutines, `StateFlow`, `SharedFlow`
- **Persistence**: [AndroidX DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 37 (Android 16)

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer recommended.
- **JDK**: Java 11 or higher.
- **Android SDK**: API Level 37 installed.

### Installation & Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Color-Palette-Generator.git
   cd Color-Palette-Generator
   ```

2. **Open in Android Studio**:
   - Open Android Studio and choose **Open an Existing Project**.
   - Select the `Color-Palette-Generator` directory.

3. **Build & Run**:
   - Connect an Android device or start an emulator (API 24+).
   - Press **Run (Shift + F10)** in Android Studio or execute via Gradle CLI:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 🧪 Running Tests

Unit tests cover color conversion math, HEX validation, and palette generation logic.

Run tests via command line:
```bash
./gradlew test
```

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git checkout -b feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📄 License

This project is open-source under the [MIT License](LICENSE).

---

## 🧑‍💻 Author

**Abdullah Ateeq**
- GitHub: [@abdullah-ateeq](https://github.com/abdullah-ateeq)
