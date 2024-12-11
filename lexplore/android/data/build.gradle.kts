plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("hnau.kotlin.multiplatform")
}

dependencies {
    kspAndroid(libs.room.compiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lexplore:data:api"))
        }
        androidMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.room.ktx)
        }
    }
}
