package hnau.lexplore.light

import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class Translator {

    private var modelIsDownloaded: Boolean = false
    private val downloadModelMutex: Mutex = Mutex()

    private val translator: Translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.GREEK)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
    )

    suspend fun translateGreekToRussian(
        greek: String,
    ): String? {
        if (!downloadModelIfNeed()) {
            return null
        }
        return try {
            translator
                .translate(greek)
                .await()
        } catch (th: Throwable) {
            Log.w("Translator", "Unable translate $greek to russian", th)
            null
        }
    }

    private suspend fun downloadModelIfNeed(): Boolean {
        if (modelIsDownloaded) {
            return true
        }
        Log.d("Translator", "Downloading translation model")
        return downloadModelMutex.withLock {
            try {
                translator.downloadModelIfNeeded().await()
                modelIsDownloaded = true
                true
            } catch (th: Throwable) {
                Log.w("Translator", "Unable download translation model", th)
                false
            }
        }
    }
}