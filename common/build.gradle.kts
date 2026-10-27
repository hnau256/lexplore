plugins {
    id(hnau.plugins.kotlin.serialization.get().pluginId)
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.kmpAndroid.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":fsrs"))
                implementation(hnau.kotlinx.atomicfu)
                implementation(hnau.kotlinx.serialization.json)
            }
        }
    }
}
