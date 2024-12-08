plugins {
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.opencsv)
            implementation(project(":lexplore:data:api"))
        }
    }
}
