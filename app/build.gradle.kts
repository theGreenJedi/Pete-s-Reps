plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseStoreFile = providers.environmentVariable("PETES_REPS_RELEASE_STORE_FILE").orNull
val releaseStorePassword = providers.environmentVariable("PETES_REPS_RELEASE_STORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("PETES_REPS_RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("PETES_REPS_RELEASE_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "org.petesreps"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.petesreps"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "0.5.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseSigningConfig = if (hasReleaseSigning) {
        signingConfigs.create("release") {
            storeFile = file(releaseStoreFile!!)
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    } else {
        null
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = releaseSigningConfig
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
