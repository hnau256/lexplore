package hnau.lexplore.android

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hnau.lexplore.app.LexploreApp
import hnau.lexplore.app.SavedState
import hnau.lexplore.app.impl
import hnau.lexplore.data.impl.dictionary.AndroidDictionaryRepository
import hnau.lexplore.prefiller.impl.AndroidPrefillDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AppViewModel(
    context: Context,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob())

    val app = LexploreApp(
        scope = scope,
        dependencies = LexploreApp.Dependencies.impl(
            /*            storageFactory = Storage.Factory.dataStore(
                            scope = scope,
                            context = context,
                        ),*/
            dictionariesRepository = AndroidDictionaryRepository(
                context = context,
            ),
            prefillDataProvider = AndroidPrefillDataProvider(
                context = context,
            )
        ),
        savedState = SavedState(
            state
                .get<Bundle>(StateKey)
                ?.getString(StateKey),
        ),
    )

    init {
        state.setSavedStateProvider(StateKey) {
            Bundle().apply { putString(StateKey, app.savableState.savedState) }
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
