package hnau.lexplore.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandler
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.model.goback.fallback
import hnau.lexplore.common.model.stack.Content
import hnau.lexplore.common.model.stack.NonEmptyStack
import hnau.lexplore.common.model.stack.StackModelElements
import hnau.lexplore.common.model.stack.push
import hnau.lexplore.common.model.stack.stackGoBackHandler
import hnau.lexplore.common.model.stack.tailGoBackHandler
import hnau.lexplore.common.model.stack.tryDropLast
import hnau.lexplore.ui.model.dictionaries.DictionariesModel
import hnau.shuffler.annotations.Shuffle
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

    @Shuffle
    interface Dependencies {

        fun dictionaries(): DictionariesModel.Dependencies

        fun exercise(): ExerciseModel.Dependencies

        fun edit(): EditModel.Dependencies
    }

    private val stack: StateFlow<NonEmptyStack<MainStackElementModel>> = run {
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

    @Shuffle
    interface ContentDependencies {

        fun exerice(): ExerciseModel.ContentDependencies

        fun dictionaries(): DictionariesModel.ContentDependencies

        fun edit(): EditModel.ContentDependencies
    }

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        stack.Content(
            extractKey = { element ->
                when (element) {
                    is MainStackElementModel.Dictionaries -> 0
                    is MainStackElementModel.Exercise -> 1
                    is MainStackElementModel.Edit -> 2
                }
            }
        ) { element ->
            when (element) {
                is MainStackElementModel.Exercise -> element.exercise.Content(
                    dependencies = remember(dependencies) { dependencies.exerice() }
                )

                is MainStackElementModel.Dictionaries -> element.dictionaries.Content(
                    dependencies = remember(dependencies) { dependencies.dictionaries() }
                )

                is MainStackElementModel.Edit -> element.edit.Content(
                    dependencies = remember(dependencies) { dependencies.edit() }
                )
            }
        }
    }
}