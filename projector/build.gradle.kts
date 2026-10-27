plugins {
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.kmpAndroidWithCompose.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":model"))
                implementation(hnau.commons.app.projector)
            }
        }
    }
}
