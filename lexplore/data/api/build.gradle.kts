plugins {
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {}
    }
}
