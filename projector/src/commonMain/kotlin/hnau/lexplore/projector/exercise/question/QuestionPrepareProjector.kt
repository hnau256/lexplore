package hnau.lexplore.projector.exercise.question

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.lexplore.model.exercise.question.QuestionPrepareModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map

class QuestionPrepareProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    model: QuestionPrepareModel,
) {


    @Pipe
    interface Dependencies {

        fun question(): QuestionProjector.Dependencies
    }

    private val question: StateFlow<Loadable<QuestionProjector>> = model
        .question
        .mapWithScope(scope) { scope, questionOrLoading ->
            questionOrLoading.map { question ->
                QuestionProjector(
                    scope = scope,
                    dependencies = dependencies.question(),
                    model = question,
                )
            }
        }

    @Composable
    fun Content() {
        question
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.rememberCrossfade(),
            ) { questionProjector ->
                questionProjector.Content()
            }
    }
}