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


-------
# Project Followup Questions

## **Architecture & Design Decisions**

**Q1: Why did you choose to place `ApiResult` in the domain layer instead of the data layer? Walk me through that decision and the trade-offs.**

**Q2: I see you're using `Flow<ApiResult<T>>` instead of just `Flow<T>` with exceptions. Explain the pros and cons of this approach. When might you use exceptions instead?**

**Q3: Your `UIState` has a single `Error` type with throwable composition rather than multiple error types like `NetworkError`, `DataError`. Defend this design choice. What are the implications?**

## **Error Handling & Resilience**

**Q4: Looking at your retry logic, you have `retryableHttpCodes: Set<Int> = setOf(500, 502, 503, 504)`. Why specifically these codes? Should 429 (Rate Limit) be included? What about 408 (Request Timeout)?**

**Q5: Your exponential backoff starts at 1 second with a 2.0 multiplier. How did you arrive at these values? In a production app serving millions of users, what would you consider?**

**Q6: I notice you check network connectivity before each API call. What happens if the network disconnects after the check but before the HTTP request? How would you handle that race condition?**

## **Testing Strategy**

**Q7: You chose to create a `FakeUserApi` instead of mocking with Mockito/MockK. Explain this decision. What are the trade-offs? When would you use mocks instead?**

**Q8: Your `FakeUserApi` is in `testFixtures`. Why not just put it in the main source set for simplicity? What problems could that cause?**

**Q9: How would you test the retry logic specifically? Show me how you'd verify that exponential backoff is working correctly.**

## **Android-Specific Concerns**

**Q10: Your `NetworkConnectivityChecker` uses `ConnectivityManager`. What happens when the app is backgrounded? Should you unregister network callbacks? How would you handle this in a real app?**

**Q11: Looking at your ViewModel, you're using `viewModelScope`. What happens to ongoing network requests when the ViewModel is cleared? Is this the behavior you want?**

**Q12: Your repository has a 1-second delay (`delay(1000)`) for "better UX testing". In production, would you keep this? How would you handle actual loading states?**

## **Performance & Memory**

**Q13: Your `RetryConfig.calculateDelay()` uses `Math.pow()`. For a high-frequency API with many concurrent requests, could this become a performance bottleneck? How would you optimize it?**

**Q14: Looking at error handling, you're creating new `ApiResult` instances for each error. In a chat app making hundreds of API calls per minute, how would you optimize memory usage?**

**Q15: Your `FakeUserApi` stores test data as instance variables. If this were used in parallel test execution, what problems might occur?**

## **Production Readiness**

**Q16: I don't see any logging in your retry logic. How would you monitor retry behavior in production? What metrics would you track?**

**Q17: Your network error messages are hardcoded in English. How would you handle localization? What about accessibility?**

**Q18: Looking at your DI setup, all your network components are Singletons. Is this always appropriate? When might you want different scopes?**

## **Code Review Questions**

**Q19: In `mapExceptionToApiResult()`, you have a catch-all `else -> UnknownError`. In production, what percentage of errors would you expect to hit this case? How would you monitor it?**

**Q20: Your ViewModel converts `ApiResult` to user-friendly messages. Should this logic be in the ViewModel or would you put it elsewhere? Why?**

---
