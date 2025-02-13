package hnau.lexplore.exercise.dto.dictionary

class Dictionaries(
    private val dictionaries: Map<DictionaryName, Dictionary>,
) {

    val names: List<DictionaryName>
            by lazy { dictionaries.keys.toList().sorted() }

    operator fun get(
        name: DictionaryName,
    ): Dictionary = dictionaries.getValue(
        key = name,
    )

    operator fun plus(
        other: Dictionaries,
    ): Dictionaries = Dictionaries(
        dictionaries = dictionaries + other.dictionaries,
    )

    companion object {

        val empty = Dictionaries(emptyMap())
    }

}