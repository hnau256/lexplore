plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":lexplore:model:init:api"))
        }
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":lexplore:model:common"))
            implementation(project(":lexplore:model:init:api"))
            implementation(project(":lexplore:model:init:impl"))
        }
    }
}
