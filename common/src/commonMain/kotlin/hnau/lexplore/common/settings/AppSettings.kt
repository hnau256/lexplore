package hnau.lexplore.common.settings

interface AppSettings {

    operator fun get(
        key: String,
    ): Setting<String>
}
