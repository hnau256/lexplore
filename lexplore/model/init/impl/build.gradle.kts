plugins {
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":lexplore:model:init:api"))
            implementation(project(":lexplore:model:mainstack:api"))
            implementation(project(":lexplore:data:api"))
        }
    }
}
