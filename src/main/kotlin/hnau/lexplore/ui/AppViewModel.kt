package hnau.lexplore.ui

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hnau.lexplore.common.kotlin.mapper.toMapper
import hnau.lexplore.ui.model.InitModel
import hnau.lexplore.ui.model.impl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json

class AppViewModel(
    context: Context,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob())

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val modelSkeletonMapper =
        json.toMapper(InitModel.Skeleton.serializer())

    private val initSkeleton = state
        .get<Bundle>(StateKey)
        ?.getString(StateKey)
        ?.let(modelSkeletonMapper.direct)
        ?: InitModel.Skeleton()

    val initModel = InitModel(
        scope = scope,
        dependencies = InitModel.Dependencies.impl(
            context = context,
        ),
        skeleton = initSkeleton,
    )

    init {
        state.setSavedStateProvider(StateKey) {
            Bundle().apply {
                putString(
                    StateKey,
                    modelSkeletonMapper.reverse(initSkeleton),
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    companion object {

        private const val StateKey = "state"

        fun factory(
            context: Context,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                AppViewModel(
                    context = context,
                    state = savedStateHandle,
                )
            }
        }
    }
}
