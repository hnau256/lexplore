package hnau.lexplore.android

import android.os.Bundle
import java.io.BufferedReader
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.lifecycle.lifecycleScope
import hnau.lexplore.app.LEXploreAppDependencies
import hnau.lexplore.app.createAppProjector
import hnau.lexplore.app.createLEXploreAppSeed
import hnau.lexplore.app.impl
import hnau.lexplore.data.AndroidAppDataFactory
import hnau.lexplore.model.RootModel
import org.hnau.commons.kotlin.ifTrue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.hnau.commons.app.model.app.AppViewModel
import org.hnau.commons.app.model.app.getForAndroid
import org.hnau.commons.app.model.theme.palette.SystemPalettes

class AppActivity : ComponentActivity() {

    private val speaker by lazy {
        AndroidSpeaker(
            context = applicationContext,
        )
    }

    private val viewModel: AppViewModel<RootModel, RootModel.Skeleton> by viewModels {
        AppViewModel.factory(
            seed = createLEXploreAppSeed(
                dependencies = LEXploreAppDependencies.impl(
                    speaker = speaker,
                    dictionariesJson = loadDictionariesJson(),
                    appDataFactory = AndroidAppDataFactory(
                        context = applicationContext,
                    ),
                ),
            ),
        )
    }

    private val goBackHandler: StateFlow<(() -> Unit)?>
        get() = viewModel.appModel.model.goBackHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initOnBackPressedDispatcherCallback()
        val projector = createAppProjector(
            scope = lifecycleScope,
            model = viewModel.appModel,
            createSystemPalettes = SystemPalettes.getForAndroid(
                context = this,
            ),
        )
        setContent {
            projector.Content(
                contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
            )
        }
    }


    override fun onDestroy() {
        isFinishing.ifTrue(speaker::close)
        super.onDestroy()
    }

    private fun initOnBackPressedDispatcherCallback() {
        val callback = object : OnBackPressedCallback(
            enabled = goBackHandler.value != null,
        ) {
            override fun handleOnBackPressed() {
                goBackHandler.value?.invoke()
            }
        }
        lifecycleScope.launch {
            goBackHandler
                .map { it != null }
                .distinctUntilChanged()
                .collect { goBackIsAvailable ->
                    callback.isEnabled = goBackIsAvailable
                }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun loadDictionariesJson(): String = resources
        .openRawResource(R.raw.dictionaries)
        .bufferedReader()
        .use(BufferedReader::readText)
}
