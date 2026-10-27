package hnau.lexplore.projector.exercise.question

import androidx.compose.runtime.Composable
import hnau.lexplore.projector.exercise.question.error.ErrorProjector
import hnau.lexplore.projector.exercise.question.input.InputProjector
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant

@SealUp(
    variants = [
        Variant(
            type = InputProjector::class,
            identifier = "input",
        ),
        Variant(
            type = ErrorProjector::class,
            identifier = "error",
        ),
    ],
    wrappedValuePropertyName = "projector",
    sealedInterfaceName = "QuestionStateProjector",
)
interface QuestionStateProjectorElement {

    @Composable
    fun Content()

    companion object
}
