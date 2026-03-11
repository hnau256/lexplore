package hnau.lexplore.ui.model.template

import androidx.compose.runtime.Composable
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class TemplateProjector(
    private val scope: CoroutineScope,
    private val model: TemplateModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {

    }
}