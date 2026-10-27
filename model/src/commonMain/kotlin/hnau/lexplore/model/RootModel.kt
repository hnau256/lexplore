package hnau.lexplore.model

import hnau.lexplore.common.AppDataFactory
import hnau.lexplore.common.dictionary.Dictionaries
import hnau.lexplore.common.dictionary.provider.DictionariesProvider
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.settings.AppSettings
import hnau.lexplore.common.speaker.Speaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.LoadableStateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.fold

class RootModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val appDataFactory: AppDataFactory

        val dictionariesJson: String

        val speaker: Speaker

        fun mainStack(
            knowledgeRepository: KnowledgeRepository,
            appSettings: AppSettings,
            dictionaries: Dictionaries,
        ): MainStackModel.Dependencies

        companion object
    }

    @Serializable
    data class Skeleton(
        val mainStack: MainStackModel.Skeleton = MainStackModel.Skeleton(),
    )

    val mainStack: StateFlow<Loadable<MainStackModel>> = LoadableStateFlow(scope) {
        val appData = dependencies.appDataFactory.create()
        val dictionaries = DictionariesProvider.load(dependencies.dictionariesJson)
        MainStackModel(
            scope = scope,
            skeleton = skeleton.mainStack,
            dependencies = dependencies.mainStack(
                knowledgeRepository = appData.knowledgeRepository,
                appSettings = appData.appSettings,
                dictionaries = dictionaries,
            ),
        )
    }

    val goBackHandler: GoBackHandler = mainStack.flatMapState(scope) { mainStackOrLoading ->
        mainStackOrLoading.fold(
            ifLoading = { NeverGoBackHandler },
            ifReady = MainStackModel::goBackHandler,
        )
    }
}
