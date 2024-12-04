package hnau.lexplore.light

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val logger = KotlinLogging.logger {}

class TTS(
    private val context: Context,
) {

    private val activeIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    private var tts: TextToSpeech? = null
    private val initializationMutex: Mutex = Mutex()

    suspend fun speek(
        greekText: String,
    ): Boolean {
        val id = UUID.randomUUID().toString()
        val tts = initializeIfNeed() ?: return false
        val startSpeakStatus = tts.speak(
            greekText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            id
        )
        if (startSpeakStatus != TextToSpeech.SUCCESS) {
            logger.warn { "Unable speak $greekText" }
            return false
        }
        activeIds.first { activeIds -> id !in activeIds }
        delay(1000)
        return true
    }

    private suspend fun initializeIfNeed(): TextToSpeech? = initializationMutex.withLock {
        var result = tts
        if (result == null) {
            logger.debug { "Initializing" }
            result = suspendCoroutine { continuation ->
                var textToSpeechLocal: TextToSpeech? = null
                textToSpeechLocal = TextToSpeech(context) { status ->
                    continuation.resume(
                        textToSpeechLocal.takeIf { status == TextToSpeech.SUCCESS }
                    )
                }
            }
            if (result == null) {
                logger.warn { "Error while initializing TextToSpeech" }
                return@withLock null
            }
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
            tts = result
        }
        result
    }

    private inline fun updateActiveIds(
        updater: (Set<String>) -> Set<String>,
    ): Unit = activeIds.update(updater)

    private fun removeActiveId(
        id: String,
    ): Unit = updateActiveIds { it - id }
}