package hnau.lexplore.ui.model.exercise.question.menu

import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.Sureness
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

class MenuModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onAnswer: (Answer) -> Unit,
    val selectedSureness: MutableStateFlow<Sureness>,
) : GoBackHandlerProvider {

    @Serializable
    /*data*/ class Skeleton

    @Shuffle
    interface Dependencies
}