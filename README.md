# InterviewReady Clean Architecture

A modern Android application built with MVVM Clean Architecture, Kotlin, and Jetpack Compose. This project serves as a clean architecture template that follows best practices for Android development.

## 🏗️ Architecture

<img width="1699" height="621" alt="Clean" src="https://github.com/user-attachments/assets/26186775-8235-4a34-9866-7f2a60fce546" />


This project follows the **MVVM Clean Architecture** pattern, organized into three main layers:

### Presentation Layer
- **UI Components**: Built with Jetpack Compose
- **ViewModels**: Handle UI logic and state management
- **Navigation**: Jetpack Navigation Compose for screen transitions
- **State Management**: Uses `StateFlow` for reactive UIs

### Domain Layer
- **Use Cases**: Business logic and operations
- **Repository Interfaces**: Define data operations
- **Domain Models**: Core business models

### Data Layer
- **Repository Implementations**: Concrete data sources
- **API Services**: Retrofit for network calls
- **Data Mappers**: Convert between data and domain models
- **Dependency Injection**: Hilt for managing dependencies

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: Retrofit
- **Asynchronous**: Kotlin Coroutines & Flow
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **State Management**: StateFlow

## 🚀 Features

- Clean separation of concerns
- Reactive UI with Jetpack Compose
- Dependency injection with Hilt
- Error handling and loading states
- Modern Android development practices
- Unit testable architecture

## 📁 Project Structure

```
interviewready/
├── app/                           # Main application module
│   ├── src/main/
│   │   ├── java/com/gauravbajaj/interviewready/
│   │   │   ├── MyApplication.kt  # Application class
│   │   │   └── di/               # Dependency Injection setup
│   │   └── res/                  # Application resources
│   └── build.gradle.kts          # App module build configuration
│
├── data/                          # Data layer module
│   ├── src/main/
│   │   ├── java/com/gauravbajaj/interviewready/data/
│   │   │   ├── api/              # API services and data sources
│   │   │   ├── di/               # Data layer DI modules
│   │   │   ├── model/            # Data models
│   │   │   └── repository/       # Repository implementations
│   │   └── res/raw/              # Mock data
│   └── build.gradle.kts          # Data module build configuration
│
├── domain/                        # Domain layer module
│   ├── src/main/
│   │   ├── java/com/gauravbajaj/interviewready/domain/
│   │   │   ├── model/            # Domain models
│   │   │   ├── repository/       # Repository interfaces
│   │   │   └── usecase/          # Use cases
│   └── build.gradle.kts          # Domain module build configuration
│
└── presentation/                  # Presentation layer module
    ├── src/main/
    │   ├── java/com/gauravbajaj/interviewready/
    │   │   ├── base/             # Base classes and utilities
    │   │   ├── components/       # Reusable UI components
    │   │   ├── home/             # Home screen components
    │   │   ├── main/             # Main activity and navigation
    │   │   ├── theme/            # App theming
    │   │   └── viewmodel/        # ViewModels
    │   └── res/                  # UI resources
    └── build.gradle.kts          # Presentation module build configuration
```

## Screenshots

<p float="left">
  <img src="https://github.com/user-attachments/assets/d4c6a742-101c-4033-b980-71df4abdc2cb" width="300" />
  <img src="https://github.com/user-attachments/assets/a1ba5323-14bf-4cfb-8acd-3548d0b47798" width="300" /> 
   <img src="https://github.com/user-attachments/assets/cca7b4dd-14ac-4ca9-80bc-5d832525d25f" width="300" />
  <img src="https://github.com/user-attachments/assets/7f5f60f9-0622-4c3f-b62a-922e9f18c8f7" width="300" />
  <img src="https://github.com/user-attachments/assets/86471ca2-627c-4df4-bd4a-41b273fb85dc" width="300" />
</p>


## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/gbajaj/interviewready-cleanarch.git
   ```
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Build and run the project

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

## 🛠 Build Variants
- **debug**: Development build with debug symbols
- **release**: Production build with ProGuard/R8 optimizations

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

Gaurav Bajaj - [@gbajaj](https://github.com/gbajaj)

Project Link: [https://github.com/gbajaj/interviewready-cleanarch.git](https://github.com/gbajaj/interviewready-cleanarch)
