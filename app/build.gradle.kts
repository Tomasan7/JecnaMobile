plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    kotlin("plugin.serialization") version "1.7.10"

    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "me.tomasan7.jecnamobile"
        minSdk = 21
        targetSdk = 32
        versionCode = 7
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.1"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

/* Allow references to generated code */
kapt {
    correctErrorTypes = true
}

dependencies {
    /* Main JecnaAPI dependency. */
    implementation("me.tomasan7:jecna-api:1.2.6")

    /* --- Jetpack compose --- */
    val composeVersion = rootProject.extra["compose_version"]
    val lifecycleVersion = "2.5.1"

    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    val composeMaterial3Version = "1.0.0-beta02"
    implementation("androidx.compose.material3:material3:$composeMaterial3Version")
    implementation("androidx.compose.material3:material3-window-size-class:$composeMaterial3Version")

    val accompanistVersion = "0.25.1"
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")

    implementation("androidx.compose.material:material-icons-extended:$composeVersion")

    val composeDestinationsVersion = "1.6.20-beta"
    implementation("io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion")

    /* Dagger-Hilt */
    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-android-compiler:2.43.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    /* Compose HTML */
    implementation("com.github.ireward:compose-html:1.0.2")
}