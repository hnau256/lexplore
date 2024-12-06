package hnau.lexplore.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import hnau.common.kotlin.castOrThrow
import hnau.common.kotlin.remindType
import hnau.lexplore.compose.Content
import hnau.lexplore.compose.utils.LocalizerImpl
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.projector.common.Localizer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.simple.SimpleLogger

class AppActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory(
            context = applicationContext,
        )
    }

    init {
        LoggerFactory
            .getLogger("AppActivity")
            .castOrThrow<SimpleLogger>()
            .debug("Start AppActivity")
    }

    private val goBackHandler: StateFlow<(() -> Unit)?>
        get() = viewModel.app.model.remindType<InitModel>().goBackHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initOnBackPressedDispatcherCallback()
        val localizer: Localizer = LocalizerImpl
        setContent {
            viewModel.app.Content(
                localizer = localizer,
            )
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onBackPressed() {
        if (useOnBackPressedDispatcher) {
            super.onBackPressed()
        }
        goBackHandler
            .value
            ?.invoke()
            ?: super.onBackPressed()
    }

    private fun initOnBackPressedDispatcherCallback() {
        if (!useOnBackPressedDispatcher) {
            return
        }
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

    companion object {

        private val useOnBackPressedDispatcher: Boolean = Build.VERSION.SDK_INT >= 33
    }
}
