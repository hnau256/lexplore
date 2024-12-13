package hnau.lexplore.data.api.dictionary.dto

import arrow.core.identity
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.toEnum
import java.util.Locale

enum class Language(
    val code: String,
    val preferredCountryCode: String? = null,
) {
    RU(
        code = "ru",
    ),

    EL(
        code = "el",
        preferredCountryCode = "GR",
    ),
    ;

    val locale: Locale?
        get() = localesByLanguages[this]

    companion object {

        val default: Language = Language.entries.first()

        val codeMapper: Mapper<String, Language> = Mapper.toEnum(
            default = default,
            extractValue = Language::code
        )

        private val localesByLanguages: Map<Language, Locale> = run {

            val locales: Array<out Locale> = Locale.getAvailableLocales().orEmpty()

            Language
                .entries
                .mapNotNull { language ->
                    val locale = locales
                        .filter { locale ->
                            locale.language == language.code
                        }
                        .maxByOrNull { locale ->
                            val country = locale.country
                            val languageCode = locale.language
                            when {
                                country.equals(language.preferredCountryCode, true) -> 3
                                country.equals(languageCode, true) -> 2
                                country.isNotEmpty() -> 1
                                else -> 0
                            }
                        }
                        ?: return@mapNotNull null
                    language to locale
                }
                .associate(::identity)
        }
    }
}