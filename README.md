# 🐝 HelpHive

> A community care ecosystem — mood tracking, kindness logging, and a practical neighbourhood help network

![Platform](https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-purple?style=flat-square&logo=kotlin)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-blue?style=flat-square)
![DI](https://img.shields.io/badge/DI-Hilt-orange?style=flat-square)
![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2024-blue?style=flat-square)

---

## Overview

HelpHive is a three-app ecosystem built to strengthen community bonds and combat social isolation. It addresses three interconnected dimensions of community wellbeing simultaneously — emotional awareness, prosocial behaviour, and practical support.

### The Three Apps

| App | Purpose |
|---|---|
| 🏠 **HelpHive Core** | Connects neighbours for practical day-to-day assistance |
| 🎨 **MoodMosaic** | Mood logging with aggregated neighbourhood emotional heat maps and AI support |
| 💛 **KindConnect** | Captures and surfaces everyday acts of kindness in a positive community feed |

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 Standard Auth System | Secure authentication and user account management |
| 😊 Mood Logging (8 emotions) | Log from Joy, Sadness, Anger, Anxiety, Calm, Excitement, Fatigue, Gratitude |
| 🗺️ Neighbourhood Mood Map | Aggregated community emotional heat map (privacy-preserving) |
| 🤖 AI Support Messages | Gemini AI delivers clinically-informed responses to negative mood logs |
| 🆘 Crisis Resource Access | Mental health hotline resources surfaced directly in-app |
| 💛 Kindness Feed | Positive-only community feed — no leaderboards, only intrinsic recognition |
| 🎯 Group Challenges | Neighbourhood-coordinated kindness initiatives with participation tracking |
| 📊 Mood Statistics | Personal mood history charts with MPAndroidChart |
| 📰 Community Feed | Activity feed showing local community engagement |
| 📴 Offline-First Sync | Full functionality without internet via Room + Firestore merge |
| 💉 Hilt Dependency Injection | Full DI setup across all layers |

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Architecture:** Clean Architecture (Data / Domain / Presentation)
- **Local DB:** Room Database
- **Remote DB:** Cloud Firestore
- **DI:** Hilt
- **AI:** Google Gemini API
- **Charts:** MPAndroidChart
- **Async:** Kotlin Coroutines & Flow
- **Min SDK:** API 24 (Android 7.0)

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          (Room DAOs)
│   │   └── entities/     (Room Entities)
│   ├── remote/
│   │   └── FirestoreDataSource.kt
│   └── repository/
│       ├── MoodRepositoryImpl.kt
│       ├── KindnessRepositoryImpl.kt
│       └── HelpRepositoryImpl.kt
├── domain/
│   ├── model/            (pure Kotlin data classes)
│   ├── repository/       (interfaces)
│   └── usecase/          (business logic)
└── presentation/
    ├── mood/
    ├── kindness/
    ├── help/
    └── community/
```

---

## 💡 Implementation Highlights

**Transparent Offline/Online Merge**
The repository layer merges Firestore (online) and Room (offline) data sources transparently. The UI layer never knows which source it is reading from — when offline, Room serves data instantly; when online, Firestore updates flow through and Room is updated in the background.

```kotlin
// Repository decides source — UI never touches this logic
override fun getMoodLogs(): Flow<List<MoodLog>> = flow {
    // Emit local cache immediately
    emitAll(moodDao.getAllMoodLogs().map { it.toDomain() })
    // Then sync from Firestore in background
    firestoreSource.getMoodLogs().collect { remote ->
        moodDao.insertAll(remote.toEntity())
    }
}
```

**Privacy-by-Design Mood Maps**
Community mood visualisations apply a minimum aggregation threshold — individual data points are never displayed publicly. Only patterns across a sufficient number of users are shown, so no individual's emotional state is ever identifiable from the heat map.

**Clinically-Informed AI Prompts**
When a user logs a negative emotion (Sadness, Anxiety, Anger), Gemini AI is called with a carefully crafted system prompt that includes emotional context. Crisis hotline resources are surfaced directly in-app rather than redirecting to an external browser, reducing friction for users in distress.

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h1.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h2.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h3.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h4.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h5.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h6.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h7.jpg" width="13%"/>
</p>
<p align="center">
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h8.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h9.jpg" width="13%"/>
  <img src=https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h10.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h11.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h12.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h13.jpg" width="13%"/>
  <img src="https://github.com/sudiptoroy7666-lgtm/portfolio/blob/fbc009ea41d89c1956497af02910183aa3dd1ecc/h14.jpg" width="13%"/>
</p>

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- A Firebase project with Firestore enabled
- A Gemini API key from [Google AI Studio](https://aistudio.google.com/)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/sudiptoroy7666-lgtm/helphive.git
   cd helphive
   ```

2. **Configure Firebase**
   - Add Android app to your Firebase project
   - Enable Cloud Firestore and Authentication
   - Download `google-services.json` → place in `app/`

3. **Add your API key in `local.properties`**
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔮 Future Improvements

- [ ] Verified volunteers and human moderation tools
- [ ] Anonymous mode for sensitive posts
- [ ] Integration with municipal emergency response systems
- [ ] Richer mood analytics and data export
- [ ] Voice assistant for hands-free mood logging

---

## 👤 Author

**Sudipta Roy**  
Android Developer | Java & Kotlin  
📧 sudiptoroy7666@gmail.com  
🔗 [Portfolio](https://sudiptoroy7666-lgtm.github.io/portfolio/) · [LinkedIn](https://www.linkedin.com/in/sudipta-roy-3873512b4/) · [GitHub](https://github.com/sudiptoroy7666-lgtm)
