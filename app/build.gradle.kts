plugins {
    id(hnau.plugins.ksp.get().pluginId)
    id(hnau.plugins.hnau.kmpAndroidWithCompose.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":model"))
                implementation(project(":projector"))
                implementation(project(":common"))
                implementation(hnau.commons.app.model)
                implementation(hnau.commons.app.projector)
            }
        }
    }
}
