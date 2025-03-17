package hnau.lexplore.sentence.dimension

import kotlinx.serialization.Serializable

@Serializable
enum class Person {
    First,
    Second,
    Third,
}

@Serializable
data class PersonValues<out T>(
    val first: T,
    val second: T,
    val third: T,
) {

    operator fun get(
        person: Person,
    ): T = when (person) {
        Person.First -> first
        Person.Second -> second
        Person.Third -> third
    }
}