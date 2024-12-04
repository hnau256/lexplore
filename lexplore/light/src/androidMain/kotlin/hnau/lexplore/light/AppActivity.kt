package hnau.lexplore.light

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import hnau.lexplore.light.engine.Engine

class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val translator = Translator()
        val tts = TTS(this)
        val engine = Engine(this)
        setContent {
            AppContent(
                translator = translator,
                tts = tts,
                engine = engine,
            )
        }
    }

}
