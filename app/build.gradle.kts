plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
}

android {
    compileSdk = 33

    namespace = "me.tomasan7.jecnamobile"

    defaultConfig {
        applicationId = "me.tomasan7.jecnamobile"
        minSdk = 26
        targetSdk = 32
        versionCode = 17
        versionName = "2.0.1"

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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.all {
        addJavaSourceFoldersToModel(
            File(buildDir, "generated/ksp/$name/kotlin")
        )
    }
}

/* Allow references to generated code */
kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.jecnaAPI)

    implementation(libs.compose.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)
    implementation(libs.compose.html)
    implementation(libs.compose.coil)
    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)
    implementation(libs.compose.state.events)

    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    implementation(libs.datastore)
    implementation(libs.serialization.json)
}