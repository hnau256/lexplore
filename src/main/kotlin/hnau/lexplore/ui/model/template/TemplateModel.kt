package hnau.lexplore.ui.model.template

import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.pipe.annotations.Pipe
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

    @Pipe
    interface Dependencies
}