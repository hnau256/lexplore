plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.kotlin.serialization)
    id("hnau.android.app")
}

android {
    namespace = "hnau.lexplore.light"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFile("proguard-rules.pro")
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.room.ktx)
            implementation(libs.mlkit.translate)
            implementation(libs.kotlin.coroutines.play.services)
            implementation(libs.android.activity.compose)
            implementation(libs.android.appcompat)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.arrow.core)
            implementation(libs.slf4j.simple)
            implementation(libs.slf4j.kotlin)
            implementation(compose.materialIconsExtended)
            implementation(project(":common:app"))
            implementation(project(":common:kotlin"))
            implementation(project(":common:color"))
            implementation(project(":common:compose"))
        }
    }
}