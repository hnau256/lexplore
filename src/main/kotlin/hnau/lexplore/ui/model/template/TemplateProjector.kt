package hnau.lexplore.ui.model.template

import androidx.compose.runtime.Composable
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class TemplateProjector(
    private val scope: CoroutineScope,
    private val model : TemplateModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {

    }
}