package hnau.lexplore.projector.dictionaries.impl

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import hnau.common.kotlin.remindType
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.lexplore.projector.common.Content
import hnau.lexplore.projector.common.flag
import hnau.lexplore.projector.dictionaries.api.DictionariesProjector
import kotlinx.coroutines.CoroutineScope

internal class DictionariesProjectorImpl(
    scope: CoroutineScope,
    private val dependencies: DictionariesProjector.Dependencies,
    private val model: DictionariesModel,
) : DictionariesProjector {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val dictionaries by model.dictionaries.collectAsState()
        LazyColumn {
            items(
                items = dictionaries,
                key = { dictionary ->
                    dictionary.id.remindType<DictionaryInfo.Id>().id
                },
            ) { dictionary ->
                ListItem(
                    icon = {
                        dictionary.info.mainLanguage.Content()
                    },
                ) {
                    Text(dictionary.info.name)
                }
            }
        }
    }
}
