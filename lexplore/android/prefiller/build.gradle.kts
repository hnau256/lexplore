plugins {
    alias(libs.plugins.kotlin.serialization)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.slf4j.kotlin)
            implementation(project(":lexplore:data:api"))
            implementation(project(":lexplore:prefiller:api"))
        }
    }
}
