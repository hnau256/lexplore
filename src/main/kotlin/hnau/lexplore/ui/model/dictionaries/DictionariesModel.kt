package hnau.lexplore.ui.model.dictionaries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hnau.lexplore.R
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.uikit.ScreenContent
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import hnau.lexplore.common.ui.uikit.topappbar.TopAppBarScope
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.LazyListStateSerializer
import hnau.lexplore.common.ui.utils.plus
import hnau.lexplore.data.settings.AppSettings
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class DictionariesModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val openQuestions: (Set<DictionaryName>) -> Unit,
    private val edit: (DictionaryName) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(LazyListStateSerializer::class)
        val scrollState: LazyListState = LazyListState(),

        @Serializable(MutableStateFlowSerializer::class)
        val overwrittenUnselectedDictionaries: MutableStateFlow<Set<DictionaryName>?> =
            MutableStateFlow(null),
    )

    @Shuffle
    interface Dependencies {

        val appSettings: AppSettings

        val dictionaries: Dictionaries
    }

    private val unselectedDictionariesDelegate = UnselectedDictionariesDelegate(
        scope = scope,
        settings = dependencies.appSettings,
        overwritten = skeleton.overwrittenUnselectedDictionaries,
    )

    @Shuffle
    interface ContentDependencies {

        fun screenContent(): ScreenContentDependencies
    }

    private val loadingWordsInProgressRegistry = InProgressRegistry()

    private val start: StateFlow<(() -> Unit)?> = unselectedDictionariesDelegate
        .unselectedNames
        .mapState(
            scope = scope,
        ) { unselectedDictionaries ->
            val dictionariesToStart = dependencies
                .dictionaries
                .names
                .filter { name -> name !in unselectedDictionaries }
                .takeIf { it.isNotEmpty() }
                ?: return@mapState null
            {
                scope.launch {
                    loadingWordsInProgressRegistry.executeRegistered {
                        unselectedDictionariesDelegate.apply()
                        openQuestions(dictionariesToStart.toSet())
                    }
                }
            }
        }

    private val allDictionaryNames: Set<DictionaryName> = dependencies
        .dictionaries
        .names
        .toSet()

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Title(
                    text = stringResource(R.string.dictionaries_title),
                )
                SelectButton(
                    text = stringResource(R.string.dictionaries_none),
                    targetNames = allDictionaryNames,
                )
                SelectButton(
                    text = stringResource(R.string.dictionaries_all),
                    targetNames = emptySet(),
                )
            }
        ) { contentPadding ->
            Dictionaries(
                contentPadding = contentPadding,
            )
            StartButton()
            AnimatedVisibility(
                visible = loadingWordsInProgressRegistry.isProgress.collectAsState().value,
            ) {
                ProgressIndicatorPanel()
            }
        }
    }

    @Composable
    private fun TopAppBarScope.SelectButton(
        text: String,
        targetNames: Set<DictionaryName>,
    ) {
        val unselectedDictionariesValue by unselectedDictionariesDelegate.unselectedNames.collectAsState()
        val onClick =
            remember(targetNames, unselectedDictionariesDelegate, unselectedDictionariesValue) {
                val existingTargetNames = targetNames
                    .takeIf { it != unselectedDictionariesValue }
                    ?: return@remember null
                {
                    unselectedDictionariesDelegate.update { existingTargetNames }
                }
            }
        Action(
            onClick = onClick,
            content = { Text(text) }
        )
    }

    @Composable
    private fun StartButton() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.largeSeparation),
            contentAlignment = Alignment.BottomEnd,
        ) {
            val start by start.collectAsState()
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.dictionaries_start)) },
                icon = { Icon { RocketLaunch } },
                onClick = { start?.invoke() },
                containerColor = when (start) {
                    null -> MaterialTheme.colorScheme.surface
                    else -> FloatingActionButtonDefaults.containerColor
                },
                expanded = !skeleton.scrollState.canScrollBackward,
            )
        }
    }

    @Composable
    private fun Dictionaries(
        contentPadding: PaddingValues,
    ) {
        val currentUnselectedDictionaries: Set<DictionaryName> by unselectedDictionariesDelegate
            .unselectedNames
            .collectAsState()
        LazyColumn(
            state = skeleton.scrollState,
            contentPadding = contentPadding + PaddingValues(bottom = 96.dp),
        ) {
            items(
                items = dependencies
                    .dictionaries
                    .names,
                key = DictionaryName::name,
            ) { name ->
                val selected = name !in currentUnselectedDictionaries
                Dictionary(
                    name = name,
                    selected = selected,
                    setIsSelected = remember(name, unselectedDictionariesDelegate) {
                        { newSelected ->
                            unselectedDictionariesDelegate.update { current ->
                                when (newSelected) {
                                    true -> current - name
                                    false -> current + name
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun Dictionary(
        name: DictionaryName,
        selected: Boolean,
        setIsSelected: (Boolean) -> Unit,
    ) {
        ListItem(
            headlineContent = { Text(name.name) },
            leadingContent = {
                Switch(
                    checked = selected,
                    onCheckedChange = setIsSelected,
                )
            },
            trailingContent = {
                IconButton(
                    onClick = { edit(name) }
                ) {
                    Icon { Edit }
                }
            }
        )
    }
}