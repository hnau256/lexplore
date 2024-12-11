package hnau.lexplore.projector.dictionaries.impl

import androidx.compose.runtime.Composable
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.lexplore.projector.dictionaries.api.DictionariesProjector
import kotlinx.coroutines.CoroutineScope

internal class DictionariesProjectorImpl(
    scope: CoroutineScope,
    private val dependencies: DictionariesProjector.Dependencies,
    model: DictionariesModel,
) : DictionariesProjector {

    @Composable
    override fun Content() {

    }
}
