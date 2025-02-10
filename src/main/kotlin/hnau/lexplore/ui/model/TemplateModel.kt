package hnau.lexplore.ui.model

import androidx.compose.runtime.Composable
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class TemplateModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val a: Int = 0,
    )

    @Shuffle
    interface Dependencies {

    }

    @Shuffle
    interface ContentDependencies {

    }

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        TODO()
    }
}