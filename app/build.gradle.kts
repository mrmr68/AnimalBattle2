import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Read an optional gradle property, defaulting to empty string.
fun propertyOrEmpty(name: String): String =
    (project.findProperty(name) as String?) ?: ""

// Load version.properties (single source of truth for release versioning).
val versionProps = Properties().apply {
    val f = rootProject.file("app/version.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.animalbattle.game"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.animalbattle.game"
        minSdk = 24
        targetSdk = 34
        versionCode = versionProps.getProperty("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = versionProps.getProperty("VERSION_NAME") ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resConfigs("en", "fa", "ar")

        // Backend base URL; blank = remote sync disabled (offline mock only).
        // Override with: ./gradlew assembleDebug -PapiBaseUrl=https://your-host
        buildConfigField("String", "API_BASE_URL", "\"${propertyOrEmpty("apiBaseUrl")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Lottie Animations
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    // (Room and Hilt available via GameConfig/DataStore if needed later)

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.json:json:20231013")
}
