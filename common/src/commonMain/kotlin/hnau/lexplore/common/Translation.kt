package hnau.lexplore.common

import kotlin.jvm.JvmInline

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Translation(
    val translation: String,
)
