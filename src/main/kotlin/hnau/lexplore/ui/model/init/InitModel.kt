package hnau.lexplore.ui.model.init

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.kotlin.LoadableStateFlow
import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.getOrInit
import hnau.lexplore.common.kotlin.toAccessor
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.model.goback.NeverGoBackHandler
import hnau.lexplore.common.ui.color.buildColors
import hnau.lexplore.common.ui.color.material.MaterialHue
import hnau.lexplore.common.ui.uikit.Content
import hnau.lexplore.common.ui.uikit.backbutton.BackButtonDelegate
import hnau.lexplore.common.ui.uikit.backbutton.BackButtonWidthProvider
import hnau.lexplore.common.ui.uikit.bubble.BubblesShower
import hnau.lexplore.common.ui.uikit.bubble.Content
import hnau.lexplore.common.ui.uikit.bubble.SharedBubblesHolder
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.data.db.AppDatabase
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.data.settings.AppSettings
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.Dictionary
import hnau.lexplore.ui.model.mainstack.MainStackModel
import hnau.lexplore.utils.TTS
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class InitModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        var mainStack: MainStackModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        val context: Context

        fun mainStack(
            knowledgeRepository: KnowledgeRepository,
            appSettings: AppSettings,
            dictionaries: Dictionaries,
            tts: TTS,
        ): MainStackModel.Dependencies

        companion object
    }

    private val tts = TTS(
        context = dependencies.context,
        scope = scope,
    )

    private data class Initialized(
        val knowledgeRepository: KnowledgeRepository,
        val appSettings: AppSettings,
        val dictionaries: Dictionaries,
    )

    val mainStackModel: StateFlow<Loadable<MainStackModel>> = LoadableStateFlow(scope) {
        coroutineScope {
            val database = AppDatabase.create(
                context = dependencies.context,
            )
            val knowledgeRepositoryDeferred = async {
                KnowledgeRepository.create(
                    database = database,
                )
            }
            val appSettingsDeferred = async {
                AppSettings.create(
                    database = database,
                )
            }
            val dictionaries = async {
                Dictionary.loadList(
                    context = dependencies.context
                )
            }
            Initialized(
                knowledgeRepository = knowledgeRepositoryDeferred.await(),
                appSettings = appSettingsDeferred.await(),
                dictionaries = dictionaries.await(),
            )
        }
    }
        .mapWithScope(scope) { dataRepositoryScope, dataRepositoryOrLoading ->
            dataRepositoryOrLoading.map { initialized ->
                MainStackModel(
                    scope = dataRepositoryScope,
                    dependencies = dependencies.mainStack(
                        knowledgeRepository = initialized.knowledgeRepository,
                        appSettings = initialized.appSettings,
                        dictionaries = initialized.dictionaries,
                        tts = tts,
                    ),
                    skeleton = skeleton::mainStack
                        .toAccessor()
                        .getOrInit(MainStackModel::Skeleton),
                )
            }
        }

    override val goBackHandler: StateFlow<(() -> Unit)?> = mainStackModel
        .flatMapState(scope) { currentMainStackModel ->
            currentMainStackModel.fold(
                ifLoading = { NeverGoBackHandler },
                ifReady = GoBackHandlerProvider::goBackHandler,
            )
        }
}