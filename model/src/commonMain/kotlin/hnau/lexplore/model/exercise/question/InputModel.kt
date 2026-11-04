@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.lexplore.model.exercise.question

import hnau.lexplore.fsrs.dto.FsrsRating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.gen.enumvalues.annotations.EnumValues
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrInProgressIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class InputModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    onReady: suspend (
        rating: FsrsRating,
        input: String,
    ) -> Unit,
    val onUnknown: () -> Unit,
) {

    @Fold(
        nonEmptyEntries = true,
    )
    @EnumValues
    enum class KnownFsrsRating {
        Hard, Good, Easy;

        val base: FsrsRating
            get() = fold(
                ifHard = { FsrsRating.Hard },
                ifGood = { FsrsRating.Good },
                ifEasy = { FsrsRating.Easy },
            )

        companion object {

            val default: KnownFsrsRating
                get() = Good
        }
    }

    @Serializable
    data class Skeleton(
        val input: MutableStateFlow<String> = "".toMutableStateFlowAsInitial(),
        val selectedRating: MutableStateFlow<KnownFsrsRating> = KnownFsrsRating.default.toMutableStateFlowAsInitial(),
    )

    val input: MutableStateFlow<String>
        get() = skeleton.input

    val selectedRating: MutableStateFlow<KnownFsrsRating>
        get() = skeleton.selectedRating

    private val nonEmptyInput: StateFlow<String?> = input.mapState(scope) { input ->
        input
            .trim()
            .takeIf(String::isNotEmpty)
    }

    val enter: StateFlow<ActionOrElse<Unit, CancelOrInProgress.InProgress>?> =
        nonEmptyInput.flatMapWithScope(
            scope = scope,
        ) { scope, inputOrNull ->
            inputOrNull.foldNullable(
                ifNull = { null.toMutableStateFlowAsInitial() },
                ifNotNull = { input ->
                    actionOrInProgressIfExecuting(scope) {
                        onReady(
                            selectedRating.value.base,
                            input,
                        )
                    }
                }
            )
        }
}
