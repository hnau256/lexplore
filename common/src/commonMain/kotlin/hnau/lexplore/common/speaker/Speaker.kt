package hnau.lexplore.common.speaker

interface Speaker {

    suspend fun speak(
        text: String,
    ): Boolean
}
