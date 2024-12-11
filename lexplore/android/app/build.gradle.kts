plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.android.app")
}

android {
    namespace = "hnau.lexplore"
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
            implementation(libs.slf4j.simple)
            implementation(project(":common:app"))
            implementation(project(":common:android:app"))
            implementation(project(":lexplore:app"))
            implementation(project(":lexplore:compose"))
            implementation(project(":lexplore:model:init:api"))
            implementation(project(":lexplore:projector:common"))
            implementation(project(":lexplore:data:api"))
            implementation(project(":lexplore:data:impl"))
            implementation(project(":lexplore:prefiller:api"))
            implementation(project(":lexplore:prefiller:impl"))
        }
    }
}