package hnau.lexplore.ui.model.mainstack

import androidx.compose.runtime.Composable
import hnau.lexplore.common.ui.projector.stack.Content
import hnau.lexplore.common.ui.projector.stack.StackProjectorTail
import hnau.lexplore.ui.model.dictionaries.DictionariesProjector
import hnau.lexplore.ui.model.edit.EditProjector
import hnau.lexplore.ui.model.exercise.ExerciseProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class MainStackProjector(
    scope: CoroutineScope,
    model: MainStackModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun exercise(): ExerciseProjector.Dependencies

        fun dictionaries(): DictionariesProjector.Dependencies

        fun edit(): EditProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Any?, MainStackElementProjector>> = StackProjectorTail(
        scope = scope,
        modelsStack = model.stack,
        extractKey = { model -> model.key },
        createProjector = { scope, model ->
            when (model) {
                is MainStackElementModel.Dictionaries -> MainStackElementProjector.Dictionaries(
                    DictionariesProjector(
                        scope = scope,
                        model = model.dictionaries,
                        dependencies = dependencies.dictionaries(),
                    )
                )

                is MainStackElementModel.Edit -> MainStackElementProjector.Edit(
                    EditProjector(
                        scope = scope,
                        model = model.edit,
                        dependencies = dependencies.edit(),
                    )
                )

                is MainStackElementModel.Exercise -> MainStackElementProjector.Exercise(
                    ExerciseProjector(
                        scope = scope,
                        model = model.exercise,
                        dependencies = dependencies.exercise(),
                    )
                )
            }
        }
    )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}