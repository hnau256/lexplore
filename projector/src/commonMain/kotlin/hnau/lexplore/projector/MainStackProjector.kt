package hnau.lexplore.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.lexplore.model.MainStackElementModel
import hnau.lexplore.model.fold
import hnau.lexplore.model.MainStackModel
import hnau.lexplore.projector.dictionaries.DictionariesProjector
import hnau.lexplore.projector.exercise.ExerciseProjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant

class MainStackProjector(
    scope: CoroutineScope,
    model: MainStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun dictionaries(): DictionariesProjector.Dependencies

        fun exercise(): ExerciseProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = DictionariesProjector::class,
                identifier = "dictionaries",
            ),
            Variant(
                type = ExerciseProjector::class,
                identifier = "exercise",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "MainStackElementProjector",
    )
    interface Element {

        @Composable
        fun Content(
            contentPadding: PaddingValues,
        )

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, MainStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = MainStackElementModel::ordinal,
            createProjector = { projectorScope, elementModel ->
                elementModel.fold(
                    ifDictionaries = { dictionariesModel ->
                        Element.dictionaries(
                            DictionariesProjector(
                                model = dictionariesModel,
                                dependencies = dependencies.dictionaries(),
                            )
                        )
                    },
                    ifExercise = { exerciseModel ->
                        Element.exercise(
                            ExerciseProjector(
                                scope = projectorScope,
                                model = exerciseModel,
                                dependencies = dependencies.exercise(),
                            )
                        )
                    },
                )
            }
        )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        tail.Content { elementProjector ->
            elementProjector.Content(
                contentPadding = contentPadding,
            )
        }
    }
}
