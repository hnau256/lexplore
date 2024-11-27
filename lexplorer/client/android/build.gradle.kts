plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.android.app")
}

android {
    namespace = "hnau.lexplorer"
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

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.android.activity.compose)
            implementation(libs.android.appcompat)
            implementation(libs.android.datastore)
            implementation(libs.slf4j.simple)
            implementation(project(":common:app"))
        }
    }
}