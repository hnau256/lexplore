package hnau.lexplore

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import hnau.lexplore.ui.AppViewModel
import hnau.lexplore.ui.model.init.InitProjector
import hnau.lexplore.ui.model.init.impl
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory(
            context = applicationContext,
        )
    }

    private val goBackHandler: StateFlow<(() -> Unit)?>
        get() = viewModel.initModel.goBackHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        val a = listOf(0f).sorted().let { partsCounts ->
            val count = partsCounts.size
            when (count % 2) {
                0 -> (partsCounts[count / 2] + partsCounts[count / 2 + 1]) / 2
                else -> partsCounts[count / 2]
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initOnBackPressedDispatcherCallback()
        val projector = InitProjector(
            scope = lifecycleScope,
            model = viewModel.initModel,
            dependencies = InitProjector.Dependencies.impl(
                context = this,
            )
        )
        setContent {
            projector.Content()
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onBackPressed() {
        if (useOnBackPressedDispatcher) {
            super.onBackPressed()
            return
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