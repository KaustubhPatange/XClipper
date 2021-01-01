# XClipper for Android

![build](https://github.com/KaustubhPatange/XClipper/workflows/Android%20CI/badge.svg)

> An Android XClipper client to communicate with the desktop application to simulate the sharing of clipboard activity.

<img width="120px" src="../XClipper.Web/images/icon-mobile.png"/>

## Download

<a target="_blank" href="https://play.google.com/store/apps/details?id=com.kpstv.xclipper"><img width="175px" src="https://camo.githubusercontent.com/f9dc78b44989eb93046dee0cc745b113ae8f9c2c/68747470733a2f2f7777772e62696e672e636f6d2f74683f69643d4f49502e614b56796e464857494546775079454c6b416473775148614353267069643d4170692672733d31"/></a>

## Compilation Guide

Since this project involves **license** system 📃 some of the source files has been ignored. [Read this guide](https://kaustubhpatange.github.io/XClipper/docs/#/compile) in order to compile 🗃 the project.

## Project Libraries

- [CustomWebviewTabs](https://github.com/KaustubhPatange/CustomWebviewTabs) - An alternative for CustomTabs in Android, works without the need for a service provider.
- [Realtime Extensions](https://github.com/KaustubhPatange/firebase-realtime-extensions) - A set of Kotlin extensions for realtime database to seamlessly suspend the callback listeners.

## Built with 🛠

- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.
- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - For asynchronous and more..
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes.
  - [Room](https://developer.android.com/topic/libraries/architecture/room) - SQLite object mapping library.
  - [Paging](https://developer.android.com/topic/libraries/architecture/paging) - Library helps you load and display small chunks of data at a time. Loading partial data on demand reduces usage of network bandwidth and system resources.
  - [Workmanager](https://developer.android.com/topic/libraries/architecture/workmanager) - An API that makes it easy to schedule deferrable, asynchronous tasks that are expected to run even if the app exits or the device restarts.
- [Navigation Component](https://developer.android.com/guide/navigation) - Jetpack's recommended way to implement navigation aiming to improve user experience.
- [Dependency Injection](https://developer.android.com/training/dependency-injection) -
  - [Hilt-Dagger](https://dagger.dev/hilt/) - Standard library to incorporate Dagger dependency injection into an Android application.
  - [Hilt-ViewModel](https://developer.android.com/training/dependency-injection/hilt-jetpack) - DI for injecting ViewModel.
  - [Kodein](https://kodein.org/Kodein-DI/) - A simple Kotlin dependency retrieval container (last commit [here](https://github.com/KaustubhPatange/XClipper/tree/6dff71eece38ae1b3384f96a7c169ebabe007a86)).
- [Retrofit](https://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.
- [Material Components for Android](https://github.com/material-components/material-components-android) - Modular and customizable Material Design UI components for Android.
- [Firebase](https://firebase.google.com) - A suite of tools that helps you quickly develop high-quality apps.
- [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) - For writing Gradle build scripts using Kotlin.

## License

- [The Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)

```
Copyright 2020 Kaustubh Patange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
