package hnau.lexplore.ui.model.exercise

import kotlinx.coroutines.flow.StateFlow

data class AutoTTS(
    val active: StateFlow<Boolean>,
)