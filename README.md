# 🦁 Animal Battle - Professional 2D Android Game

A premium 2D cartoon-style Android game built with Kotlin, Jetpack Compose, and Clean Architecture.

## 🎮 Features

### Core Gameplay
- **Battle System**: Turn-based combat with Power, XP, and Abilities
- **12 Unique Animals**: Lion, Tiger, Leopard, Cheetah, Bear, Wolf, Gorilla, Rhino, Elephant, Crocodile, Eagle, Cobra
- **3 Abilities per Animal**: Low, Medium, and High power abilities
- **Question System**: Farsi/Persian trivia questions with 15-second timer
- **Lifelines**: 50/50 and Skip options (cost coins)

### Game Systems
- **Lucky Wheel**: Spin to win Coins, Trophies, and Items
- **Daily Login**: 7-day reward cycle with streak tracking
- **Shop**: Data-driven shop with Coin Packs, Upgrades, Skins, and Items
- **Map**: Progressive level system with locked/available/completed states
- **Leaderboard**: Weekly ranking system (mock/local, ready for backend)
- **Profile**: Player stats, level progression, and animal selection

### Technical Features
- **MVVM + Clean Architecture**: Modular and maintainable codebase
- **Jetpack Compose**: Modern declarative UI
- **DataStore**: Persistent game state
- **RTL Support**: Full support for Persian (Farsi) and Arabic
- **Localization**: English, Persian, and Arabic languages
- **Responsive Design**: Works on all screen sizes and aspect ratios
- **Animations**: Smooth transitions and interactions

## 📁 Project Structure

```
app/src/main/java/com/animalbattle/game/
├── domain/
│   └── model/          # Data models (Player, Animal, Ability, etc.)
├── data/
│   ├── datastore/      # DataStore persistence
│   └── repository/     # Repository pattern
├── ui/
│   ├── theme/          # Colors, Typography, Theme
│   ├── components/     # Reusable UI components
│   ├── home/           # Home screen
│   ├── animals/        # Animal selection screen
│   ├── battle/         # Battle system screens
│   ├── shop/           # Shop screen
│   ├── luckywheel/     # Lucky Wheel screen
│   ├── profile/        # Profile screen
│   ├── map/            # Map/Level progression
│   ├── recentbattles/  # Battle history
│   ├── settings/       # Settings screen
│   └── leaderboard/    # Weekly leaderboard
├── viewmodel/          # GameViewModel
├── navigation/         # Navigation graph
└── audio/              # AudioManager
```

## 🎨 Design System

### Color Palette
- **Cream/Light Cream**: Primary background
- **Gold**: Borders, accents, and highlights
- **Dark Green (#114C44)**: Limited use for specific elements
- **White**: Text and clean backgrounds

### Typography
- Clean, readable fonts
- Consistent hierarchy
- Support for RTL languages

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Installation
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project directory
4. Sync Gradle files
5. Run on emulator or physical device

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## 🎯 Game Configuration

All game parameters are configurable in `GameConfig.kt`:

```kotlin
object GameConfig {
    // Battle Configuration
    const val INITIAL_PLAYER_POWER = 3
    const val INITIAL_OPPONENT_POWER = 4
    const val QUESTION_TIME_SECONDS = 15
    
    // Lifeline Costs
    const val FIFTY_FIFTY_COST = 50
    const val SKIP_COST = 30
    
    // Lucky Wheel
    const val LUCKY_WHEEL_SPIN_COST = 100
    const val MAX_DAILY_SPINS = 3
    
    // Daily Login
    const val DAILY_LOGIN_DAYS = 7
}
```

## 🐾 Adding New Animals

1. Add animal string resources in `res/values/strings.xml`
2. Add translations in `res/values-fa/strings.xml` and `res/values-ar/strings.xml`
3. Create animal in `AnimalData.kt`:

```kotlin
private fun createNewAnimal() = Animal(
    id = "new_animal",
    nameResId = R.string.animal_new_animal,
    baseHp = 95,
    basePower = 6,
    unlockCost = 500,
    upgradeCost = 250,
    abilities = listOf(
        Ability("new_ability_1", R.string.ability_1, R.string.ability_1, 12, 1),
        Ability("new_ability_2", R.string.ability_2, R.string.ability_2, 22, 2),
        Ability("new_ability_3", R.string.ability_3, R.string.ability_3, 35, 3)
    )
)
```

4. Add to `getAllAnimals()` list

## 🎨 Adding Custom Graphics

Replace placeholder assets in `res/drawable/`:

- `ic_animal_lion.png` - Lion avatar
- `ic_animal_tiger.png` - Tiger avatar
- ... (one per animal)

Asset naming convention:
- `ic_animal_[name].png` - Animal avatars
- `ic_ability_[name].png` - Ability icons
- `ic_ui_[name].png` - UI elements
- `bg_[screen].png` - Background images

## 🌍 Localization

### Adding New Languages
1. Create `res/values-[language_code]/strings.xml`
2. Translate all string resources
3. Add language option in `SettingsScreen.kt`

### RTL Support
- Persian and Arabic are fully supported
- Layout automatically adjusts for RTL
- All text alignment is handled by Compose

## 🔧 Architecture

### MVVM Pattern
- **Model**: Data classes and business logic
- **View**: Jetpack Compose UI
- **ViewModel**: State management and business logic

### Clean Architecture Layers
1. **Domain Layer**: Models and business rules
2. **Data Layer**: Repositories and DataStore
3. **Presentation Layer**: UI and ViewModels

### State Management
- `StateFlow` for reactive state
- `MutableStateFlow` for state updates
- `collectAsState()` in Compose

## 📱 Performance

- Efficient recomposition with stable keys
- Lazy lists for scrollable content
- Minimal object allocation in loops
- Optimized animations
- DataStore for efficient persistence

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Android Tests
```bash
./gradlew connectedAndroidTest
```

## 📝 License

This project is proprietary software.

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📞 Support

For support, email support@animalbattle.game

## 🎮 Game Tips

- **Power Management**: Balance between increasing power and attacking
- **XP Conservation**: Save XP for powerful abilities
- **Lifeline Strategy**: Use 50/50 for difficult questions
- **Daily Login**: Don't miss daily rewards for streak bonuses
- **Upgrade Animals**: Stronger animals have better stats

---

**Built with ❤️ using Kotlin and Jetpack Compose**
