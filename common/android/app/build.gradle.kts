plugins {
    alias(libs.plugins.kotlin.serialization)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.android.datastore)
            implementation(project(":common:app"))
        }
    }
}
