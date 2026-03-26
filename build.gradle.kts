import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.apply

plugins {
    val kotlinVersion = "2.3.10"
    id("androidx.room") version "2.7.2"
    id("com.android.application") version "8.9.3"
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.google.gms.google-services") version "4.4.3"
    kotlin("android") version kotlinVersion
}

apply("scripts/build-dictionaries-resource.gradle.kts")

tasks.named("preBuild").configure {
    dependsOn("buildDictionariesResource")
}

android {
    namespace = "hnau.lexplore"
    compileSdk = 36

    defaultConfig {
        applicationId = "hnau.lexplore"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val versionPropsFile = file("version.properties")
        val versionProps = Properties().apply {
            load(versionPropsFile.inputStream())
        }
        val localVersionCode = (versionProps["versionCode"] as String).toInt()
        versionName = versionProps["versionName"] as String + "." + localVersionCode
        versionCode = localVersionCode

        tasks.named("preBuild") {
            doFirst {
                versionProps.setProperty("versionCode", (localVersionCode + 1).toString())
                versionProps.store(versionPropsFile.outputStream(), null)
            }
        }
    }

    signingConfigs {
        create("qa") {
            storeFile = file("keystores/qa.keystore")
            storePassword = "password"
            keyAlias = "qa"
            keyPassword = "password"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFile("proguard-rules.pro")
            //signingConfig = signingConfigs.getByName("release")
        }
        create("qa") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("qa")
            applicationIdSuffix = ".qa"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))

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

    val arrow = "2.2.2"
    implementation("io.arrow-kt:arrow-core:$arrow")

    val hnau = "1.4.3"
    implementation ("org.hnau.commons:kotlin:${hnau}")
    implementation("org.hnau.commons:gen-pipe-annotations:$hnau")
    ksp("org.hnau.commons:gen-pipe-processor:$hnau")

}