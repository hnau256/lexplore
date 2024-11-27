package hnau.common.compose.projector

import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.coroutines.runningFoldState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

data class StackProjectorTail<P>(
    val projector: P,
    val isNew: Boolean?,
)

@PublishedApi
internal data class TailInfo<K, P>(
    val cancel: () -> Unit,
    val key: K,
    val stackKeys: Set<K>,
    val tail: StackProjectorTail<P>,
)

@Suppress("FunctionName")
fun <M, K, P> StackProjectorTail(
    scope: CoroutineScope,
    modelsStack: StateFlow<NonEmptyStack<M>>,
    extractKey: M.() -> K,
    createProjector: (CoroutineScope, M) -> P,
): StateFlow<StackProjectorTail<P>> {

    fun NonEmptyStack<M>.keys(): Set<K> = all.map(extractKey).toSet()

    fun createTailInfo(
        isNew: Boolean?,
        keys: Set<K>,
        model: M,
    ): TailInfo<K, P> {
        val projectorScope = scope.createChild()
        return TailInfo(
            cancel = { projectorScope.cancel() },
            tail = StackProjectorTail(
                projector = createProjector(projectorScope, model),
                isNew = isNew,
            ),
            stackKeys = keys,
            key = model.extractKey(),
        )
    }

    return modelsStack
        .runningFoldState(
            scope = scope,
            createInitial = { stack ->
                createTailInfo(
                    isNew = null,
                    keys = stack.keys(),
                    model = stack.tail,
                )
            },
            operation = { previousInfo, newStack ->
                val model = newStack.tail
                val key = model.extractKey()
                val keys = newStack.keys()
                if (previousInfo.key == key) {
                    return@runningFoldState previousInfo.copy(
                        stackKeys = keys,
                    )
                }
                previousInfo.cancel()
                createTailInfo(
                    isNew = key !in previousInfo.stackKeys,
                    keys = keys,
                    model = model,
                )
            }
        )
        .mapStateLite { it.tail }
}
