plugins {
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lexplore:data:api"))
            implementation(project(":lexplore:prefiller:api"))
        }
        androidMain.dependencies {
        }
    }
}
