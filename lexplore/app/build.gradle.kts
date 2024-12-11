plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":lexplore:model:common"))
            implementation(project(":lexplore:model:init:api"))
            implementation(project(":lexplore:model:init:impl"))
            implementation(project(":lexplore:data:api"))
        }
    }
}
dependencies {
    implementation(project(":lexplore:data:api"))
}
