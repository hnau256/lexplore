plugins {
    val kotlinVersion = "2.1.20"
    id("androidx.room") version "2.7.2"
    id("com.android.application") version "8.9.3"
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
    id("com.google.devtools.ksp") version "2.1.20-1.0.31"
    kotlin("android") version kotlinVersion
}

apply("scripts/build-dictionaries-resource.gradle.kts")

tasks.named("preBuild").configure {
    dependsOn("buildDictionariesResource")
}

android {
    namespace = "hnau.lexplore"
    compileSdk = 35

    defaultConfig {
        applicationId = "hnau.lexplore"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        create("composeTest") {
            signingConfig = signingConfigs["debug"]
            isDebuggable = false
            /*isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )*/
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")

    val composeUi = "1.8.3"
    implementation("androidx.compose.ui:ui:$composeUi")
    implementation("androidx.compose.ui:ui-tooling:$composeUi")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:$composeUi")
    implementation("androidx.compose.foundation:foundation:$composeUi")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    val room = "2.7.2"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    val pipe = "1.0.5"
    implementation("com.github.hnau256.common-gen-pipe:annotations:$pipe")
    ksp("com.github.hnau256.common-gen-pipe:processor:$pipe")

}