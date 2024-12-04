package hnau.lexplore.light

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val words = Word.loadList(this)
        val translator = Translator()
        val tts = TTS(this)
        setContent {
            AppContent(
                words = words,
                translator = translator,
                tts = tts,
            )
        }
    }

}
