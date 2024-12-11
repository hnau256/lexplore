plugins {
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(project(":lexplore:data:api"))
            implementation(project(":lexplore:prefiller:api"))
        }
    }
}
