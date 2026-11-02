plugins {
    id("org.hnau.plugin.jvm")
    `java-gradle-plugin`
}

dependencies {
    implementation(hnau.kotlinx.serialization.json)
}

gradlePlugin {
    plugins {
        create("dictionariesResource") {
            id = "hnau.lexplore.build.dictionaries-resource"
            implementationClass = "hnau.lexplore.dictionaries.DictionariesResourcePlugin"
        }
    }
}
