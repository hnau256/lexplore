package hnau.lexplore.model

import hnau.lexplore.common.dictionary.Dictionaries
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.settings.AppSettings
import hnau.lexplore.common.speaker.Speaker
import hnau.lexplore.model.dictionaries.DictionariesModel
import hnau.lexplore.model.exercise.ExerciseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.goBackHandler
import org.hnau.commons.app.model.stack.modelsOnly
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.app.model.stack.withModels
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class MainStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val stack: MutableStateFlow<NonEmptyStack<MainStackElementSkeleton>> =
            MutableStateFlow(
                NonEmptyStack(
                    ElementSkeleton.dictionaries(
                        DictionariesModel.Skeleton(),
                    )
                )
            ),
    )

    @Pipe
    interface Dependencies {

        val appSettings: AppSettings

        val knowledgeRepository: KnowledgeRepository

        val dictionaries: Dictionaries

        val speaker: Speaker

        fun dictionaries(): DictionariesModel.Dependencies

        fun exercise(): ExerciseModel.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = DictionariesModel::class,
                identifier = "dictionaries",
            ),
            Variant(
                type = ExerciseModel::class,
                identifier = "exercise",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "MainStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = DictionariesModel.Skeleton::class,
                identifier = "dictionaries",
            ),
            Variant(
                type = ExerciseModel.Skeleton::class,
                identifier = "exercise",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "MainStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }

    private val stackWithModels = skeleton
        .stack
        .withModels(
            scope = scope,
            getKey = MainStackElementSkeleton::ordinal,
        ) { modelScope, elementSkeleton ->
            createModel(
                modelScope = modelScope,
                elementSkeleton = elementSkeleton,
            )
        }

    private fun createModel(
        modelScope: CoroutineScope,
        elementSkeleton: MainStackElementSkeleton,
    ): MainStackElementModel = elementSkeleton.fold(
        ifDictionaries = { dictionariesSkeleton ->
            Element.dictionaries(
                DictionariesModel(
                    scope = modelScope,
                    skeleton = dictionariesSkeleton,
                    dependencies = dependencies.dictionaries(),
                    openQuestions = { names ->
                        skeleton.stack.push(
                            ElementSkeleton.exercise(
                                ExerciseModel.Skeleton(
                                    dictionaries = names,
                                )
                            )
                        )
                    },
                )
            )
        },
        ifExercise = { exerciseSkeleton ->
            Element.exercise(
                ExerciseModel(
                    scope = modelScope,
                    skeleton = exerciseSkeleton,
                    dependencies = dependencies.exercise(),
                )
            )
        },
    )

    val stack: StateFlow<NonEmptyStack<MainStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = MainStackElementModel::goBackHandler,
        updateSkeletonStack = { newStack -> skeleton.stack.value = newStack },
    )
}
