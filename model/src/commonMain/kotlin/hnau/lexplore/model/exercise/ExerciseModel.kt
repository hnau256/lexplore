@file:UseSerializers(
    MutableStateFlowSerializer::class,
    OptionSerializer::class,
    NonEmptySetSerializer::class,
)

package hnau.lexplore.model.exercise

import arrow.core.getOrElse
import arrow.core.serialization.NonEmptySetSerializer
import arrow.core.serialization.OptionSerializer
import hnau.lexplore.common.dictionary.Dictionaries
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.settings.AppSettings
import hnau.lexplore.common.settings.Setting
import hnau.lexplore.common.settings.map
import hnau.lexplore.common.speaker.Speaker
import hnau.lexplore.model.exercise.question.QuestionPrepareModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.stringToBoolean
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class ExerciseModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val appSettings: AppSettings

        val knowledgeRepository: KnowledgeRepository

        val dictionaries: Dictionaries

        val speaker: Speaker

        fun question(
            autoTTS: AutoTTS,
        ): QuestionPrepareModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val question: MutableStateFlow<QuestionPrepareModel.Skeleton> = QuestionPrepareModel.Skeleton
            .createFirst()
            .toMutableStateFlowAsInitial(),
    )

    private val autoTTSSetting: Setting<Boolean> = dependencies
        .appSettings["auto_tts"]
        .map(
            scope = scope,
            mapper = Mapper.stringToBoolean,
        )

    val autoTTS: AutoTTS = AutoTTS(
        active = autoTTSSetting
            .state
            .mapState(scope) { valueOrNone ->
                valueOrNone.getOrElse { true }
            },
    )

    val switchAutoTTS: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(scope) {
            autoTTSSetting.update(!autoTTS.active.value)
        }

    val question: StateFlow<QuestionPrepareModel> = skeleton
        .question
        .mapWithScope(scope) { scope, questionSkeleton ->
            QuestionPrepareModel(
                scope = scope,
                dependencies = dependencies.question(
                    autoTTS = autoTTS,
                ),
                skeleton = questionSkeleton,
                onReady = { completedWord ->
                    skeleton.question.value = QuestionPrepareModel.Skeleton.createNext(
                        previousWord = completedWord,
                    )
                }
            )
        }

    val goBackHandler: GoBackHandler = question.flatMapState(
        scope = scope,
        transform = QuestionPrepareModel::goBackHandler,
    )
}
