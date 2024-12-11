package hnau.lexplore.model.mainstack.impl

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.fallback
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.StackModelElements
import hnau.common.app.model.stack.stackGoBackHandler
import hnau.common.app.model.stack.tailGoBackHandler
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.lexplore.model.mainstack.api.MainStackElementModel
import hnau.lexplore.model.mainstack.api.MainStackModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal class MainStackModelImpl(
    scope: CoroutineScope,
    private val skeleton: MainStackModel.Skeleton,
    private val dependencies: MainStackModel.Dependencies,
    private val dictionariesModelFactory: DictionariesModel.Factory,
) : MainStackModel {

    override val stack: StateFlow<NonEmptyStack<MainStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            skeletonsStack = stack,
        ) { modelScope, skeleton ->
            createModel(
                modelScope = modelScope,
                skeleton = skeleton,
            )
        }
    }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: MainStackElementModel.Skeleton,
    ): MainStackElementModel = when (skeleton) {
        is MainStackElementModel.Skeleton.Dictionaries -> MainStackElementModel.Dictionaries(
            dictionariesModelFactory.createDictionariesModel(
                scope = modelScope,
                skeleton = skeleton.dictionaries,
                dependencies = dependencies.dictionaries(),
            )
        )
    }

    override val goBackHandler: GoBackHandler = stack
        .tailGoBackHandler(scope)
        .fallback(
            scope = scope,
            fallback = skeleton.stack.stackGoBackHandler(scope),
        )
}
