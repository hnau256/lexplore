package hnau.lexplore.android

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import hnau.lexplore.common.speaker.Speaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidSpeaker(
    context: Context,
) : Speaker, AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val activeIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    @Volatile
    private var instance: TextToSpeech? = null

    private val textToSpeech: Deferred<TextToSpeech?> = scope.async {
        initialize(context = context).also { initialized ->
            instance = initialized
        }
    }

    override suspend fun speak(
        text: String,
    ): Boolean {
        val textToSpeech = textToSpeech.await() ?: return false
        val id = UUID.randomUUID().toString()
        val startSpeakStatus = textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            id,
        )
        if (startSpeakStatus != TextToSpeech.SUCCESS) {
            return false
        }
        activeIds.first { activeIds -> id !in activeIds }
        delay(SpeakDelayMillis)
        return true
    }

    override fun close() {
        instance?.shutdown()
        instance = null
        scope.cancel()
    }

    private suspend fun initialize(
        context: Context,
    ): TextToSpeech? {
        val result = suspendCoroutine<TextToSpeech?> { continuation ->
            var textToSpeech: TextToSpeech? = null
            textToSpeech = TextToSpeech(context) { status ->
                continuation.resume(
                    textToSpeech.takeIf { status == TextToSpeech.SUCCESS }
                )
            }
        } ?: return null

        result.setSpeechRate(0.75f)
        result.language = Locale("el", "GR")
        result.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) =
                    updateActiveIds { it + utteranceId }

                override fun onDone(utteranceId: String) =
                    removeActiveId(utteranceId)

                @Suppress("DeprecatedCallableAddReplaceWith")
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String) {
                    onError(utteranceId, 0)
                }

                override fun onError(utteranceId: String, errorCode: Int) =
                    removeActiveId(utteranceId)
            }
        )
        return result
    }

    private inline fun updateActiveIds(
        updater: (Set<String>) -> Set<String>,
    ): Unit = activeIds.update(updater)

    private fun removeActiveId(
        id: String,
    ): Unit = updateActiveIds { it - id }

    companion object {

        private const val SpeakDelayMillis = 1_000L
    }
}
