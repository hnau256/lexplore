package hnau.lexplore.model.exercise

import kotlinx.coroutines.flow.StateFlow

class AutoTTS(
    val active: StateFlow<Boolean>,
)
