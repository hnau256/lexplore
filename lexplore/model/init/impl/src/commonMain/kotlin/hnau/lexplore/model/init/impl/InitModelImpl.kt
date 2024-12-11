package hnau.lexplore.model.init.impl

import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.model.mainstack.api.MainStackModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

internal class InitModelImpl(
    scope: CoroutineScope,
    private val skeleton: InitModel.Skeleton,
    private val dependencies: InitModel.Dependencies,
    private val mainStackModelFactory: MainStackModel.Factory,
) : InitModel {

    override val mainStack: StateFlow<MainStackModel?> = flow {
        val storage = dependencies.dictionariesRepository.getDictionaries()
        emit(storage)
    }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
        .scopedInState(scope)
        .mapState(scope) { (dictionariesFlowScope, dictionariesFlowOrNull) ->
            dictionariesFlowOrNull?.let { dictionariesFlow ->
                mainStackModelFactory.createMainStackModel(
                    scope = dictionariesFlowScope,
                    skeleton = skeleton::mainStack
                        .toAccessor()
                        .getOrInit { MainStackModel.Skeleton() },
                    dependencies = dependencies.mainStack(
                        dictionariesFlow = dictionariesFlow,
                    ),
                )
            }
        }

    override val goBackHandler: StateFlow<(() -> Unit)?> =
        mainStack.flatMapState(scope) { mainStack ->
            mainStack
                ?.goBackHandler
                ?: NeverGoBackHandler
        }
}
