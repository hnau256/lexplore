package hnau.lexplore.projector.exercise

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.lexplore.model.ExercisePrepareModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map

class ExercisePrepareProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    model: ExercisePrepareModel,
) {


    @Pipe
    interface Dependencies {

        fun exercise(): ExerciseProjector.Dependencies
    }

    private val exercise: StateFlow<Loadable<ExerciseProjector>> = model
        .exercise
        .mapWithScope(scope) { scope, exerciseOrLoading ->
            exerciseOrLoading.map { exercise ->
                ExerciseProjector(
                    scope = scope,
                    dependencies = dependencies.exercise(),
                    model = exercise,
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        exercise
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.rememberCrossfade(),
            ) { exerciseProjector ->
                exerciseProjector.Content(
                    contentPadding = contentPadding,
                )
            }
    }
}