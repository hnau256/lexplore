package hnau.lexplore.common.model.stack

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import hnau.lexplore.common.kotlin.coroutines.mapStateLite
import hnau.lexplore.common.kotlin.coroutines.runningFoldState
import hnau.lexplore.common.ui.utils.getTransitionSpecForHorizontalSlide
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds

@Composable
fun <M, K> StateFlow<NonEmptyStack<M>>.Content(
    extractKey: (M) -> K,
    content: @Composable (M) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val tail: StateFlow<StackProjectorTail> = remember(scope, extractKey, content) {
        this
            .runningFoldState(
                scope = scope,
                createInitial = { stack ->
                    createTailInfo(
                        extractKey = extractKey,
                        isNew = null,
                        keys = stack.keys(extractKey),
                        model = stack.tail,
                        content = content,
                    )
                },
                operation = { previousInfo, newStack ->
                    val model = newStack.tail
                    val key = extractKey(model)
                    val keys = newStack.keys(extractKey)
                    if (previousInfo.key == key) {
                        return@runningFoldState previousInfo.copy(
                            stackKeys = keys,
                        )
                    }
                    createTailInfo(
                        isNew = key !in previousInfo.stackKeys,
                        keys = keys,
                        model = model,
                        extractKey = extractKey,
                        content = content,
                    )
                }
            )
            .mapStateLite { it.tail }
    }
    val currentTail by tail.collectAsState()
    AnimatedContent(
        label = "StackProjectorStack",
        modifier = Modifier.fillMaxSize(),
        targetState = currentTail,
        transitionSpec = getTransitionSpecForHorizontalSlide(
            duration = AnimationDuration,
        ) {
            when (targetState.isNew) {
                true -> AnimationSlideFactor
                false -> -AnimationSlideFactor
                null -> 0
            }
        },
    ) { (content, _) ->
        content()
    }
}

private data class StackProjectorTail(
    val content: @Composable () -> Unit,
    val isNew: Boolean?,
)

private data class TailInfo<K>(
    val key: K,
    val stackKeys: Set<K>,
    val tail: StackProjectorTail,
)

private fun <M, K> NonEmptyStack<M>.keys(
    extractKey: M.() -> K,
): Set<K> = all.map(extractKey).toSet()

private fun <M, K> createTailInfo(
    isNew: Boolean?,
    keys: Set<K>,
    extractKey: M.() -> K,
    model: M,
    content: @Composable M.() -> Unit,
): TailInfo<K> {
    return TailInfo(
        tail = StackProjectorTail(
            content = { model.content() },
            isNew = isNew,
        ),
        stackKeys = keys,
        key = model.extractKey(),
    )
}

private const val AnimationSlideFactor = 0.5
private val AnimationDuration = 0.3.seconds