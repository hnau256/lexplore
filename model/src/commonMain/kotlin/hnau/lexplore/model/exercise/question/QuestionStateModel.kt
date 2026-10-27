package hnau.lexplore.model.exercise.question

import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant

@SealUp(
    variants = [
        Variant(
            type = InputModel::class,
            identifier = "input",
        ),
        Variant(
            type = ErrorModel::class,
            identifier = "error",
        ),
    ],
    wrappedValuePropertyName = "model",
    sealedInterfaceName = "QuestionStateModel",
)
interface QuestionStateModelElement {

    companion object
}
