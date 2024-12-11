package hnau.lexplore.projector.mainstack.impl

import androidx.compose.runtime.Composable
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.compose.projector.Content
import hnau.common.compose.projector.StackProjectorTail
import hnau.common.kotlin.remindSupertype
import hnau.common.kotlin.remindType
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.lexplore.model.mainstack.api.MainStackElementModel
import hnau.lexplore.model.mainstack.api.MainStackModel
import hnau.lexplore.projector.dictionaries.api.DictionariesProjector
import hnau.lexplore.projector.mainstack.api.MainStackProjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal class MainStackProjectorImpl(
    scope: CoroutineScope,
    private val dependencies: MainStackProjector.Dependencies,
    model: MainStackModel,
    private val dictionariesProjectorFactory: DictionariesProjector.Factory,
) : MainStackProjector {

    sealed interface Element {

        @Composable
        fun Content()

        data class Dictionaries(
            private val dictionaries: DictionariesProjector,
        ) : Element {

            @Composable
            override fun Content() =
                dictionaries.Content()
        }
    }

    private val tail: StateFlow<StackProjectorTail<Element>> = StackProjectorTail(
        scope = scope,
        modelsStack = model
            .remindSupertype<_, GoBackHandlerProvider>()
            .stack,
        extractKey = {
            when (this) {
                is MainStackElementModel.Dictionaries -> 0
            }
        },
    ) { stateScope, state ->
        when (state) {
            is MainStackElementModel.Dictionaries -> Element.Dictionaries(
                dictionariesProjectorFactory.createDictionariesProjector(
                    scope = stateScope,
                    dependencies = dependencies.dictionaries(),
                    model = state.dictionaries.remindType<DictionariesModel>(),
                )
            )
        }
    }

    @Composable
    override fun Content() {
        tail.Content { Content() }
    }
}
