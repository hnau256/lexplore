package hnau.lexplore.data.api.dictionary.dto

enum class Language {
    RU,
    EL,
    ;

    companion object {

        val default: Language = Language.entries.first()
    }
}