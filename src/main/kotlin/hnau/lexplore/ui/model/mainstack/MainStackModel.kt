package hnau.lexplore.ui.model.mainstack

import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandler
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.model.goback.fallback
import hnau.lexplore.common.model.stack.NonEmptyStack
import hnau.lexplore.common.model.stack.StackModelElements
import hnau.lexplore.common.model.stack.push
import hnau.lexplore.common.model.stack.stackGoBackHandler
import hnau.lexplore.common.model.stack.tailGoBackHandler
import hnau.lexplore.common.model.stack.tryDropLast
import hnau.lexplore.ui.model.dictionaries.DictionariesModel
import hnau.lexplore.ui.model.edit.EditModel
import hnau.lexplore.ui.model.exercise.ExerciseModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class MainStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val stack: MutableStateFlow<NonEmptyStack<MainStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(MainStackElementModel.Skeleton.Dictionaries())),
    )

    @Pipe
    interface Dependencies {

        fun dictionaries(): DictionariesModel.Dependencies

        fun exercise(): ExerciseModel.Dependencies

        fun edit(): EditModel.Dependencies
    }

    val stack: StateFlow<NonEmptyStack<MainStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            skeletonsStack = stack,
        ) { modelScope, skeleton ->
            createModel(
                modelScope = modelScope,
                skeleton = skeleton,
            )
        }
    }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: MainStackElementModel.Skeleton,
    ): MainStackElementModel = when (skeleton) {
        is MainStackElementModel.Skeleton.Dictionaries -> MainStackElementModel.Dictionaries(
            DictionariesModel(
                scope = modelScope,
                skeleton = skeleton.dictionaries,
                dependencies = dependencies.dictionaries(),
                openQuestions = { words ->
                    this
                        .skeleton
                        .stack
                        .push(MainStackElementModel.Skeleton.Exercise(words))
                },
                edit = { name ->
                    this
                        .skeleton
                        .stack
                        .push(
                            MainStackElementModel.Skeleton.Edit(
                                name = name,
                            )
                        )
                }
            )
        )

        is MainStackElementModel.Skeleton.Exercise -> MainStackElementModel.Exercise(
            ExerciseModel(
                scope = modelScope,
                skeleton = skeleton.exercise,
                dependencies = dependencies.exercise(),
                goBack = { this.skeleton.stack.tryDropLast() },
            )
        )

        is MainStackElementModel.Skeleton.Edit -> MainStackElementModel.Edit(
            EditModel(
                scope = modelScope,
                skeleton = skeleton.edit,
                dependencies = dependencies.edit(),
            )
        )
    }

    override val goBackHandler: GoBackHandler = stack
        .tailGoBackHandler(scope)
        .fallback(
            scope = scope,
            fallback = skeleton.stack.stackGoBackHandler(scope),
        )
}