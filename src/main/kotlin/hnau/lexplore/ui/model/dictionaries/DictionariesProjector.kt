package hnau.lexplore.ui.model.dictionaries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import hnau.lexplore.common.ui.uikit.ScreenContent
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import hnau.lexplore.common.ui.uikit.topappbar.TopAppBarScope
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.plus
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class DictionariesProjector(
    private val scope: CoroutineScope,
    private val model: DictionariesModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun screenContent(): ScreenContentDependencies
    }

    private val update: ((Set<DictionaryName>) -> Set<DictionaryName>) -> Unit = model::update

    @Composable
    fun Content() {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Title(
                    text = stringResource(R.string.dictionaries_title),
                )
                SelectButton(
                    text = stringResource(R.string.dictionaries_none),
                    targetNames = model.allDictionaryNamesSet,
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
            StartButton(
                contentPadding = contentPadding,
            )
            AnimatedVisibility(
                visible = model.isLoadingWords.collectAsState().value,
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
        val unselectedDictionariesValue by model.unselectedNames.collectAsState()
        val onClick =
            remember(targetNames, update, unselectedDictionariesValue) {
                val existingTargetNames = targetNames
                    .takeIf { it != unselectedDictionariesValue }
                    ?: return@remember null
                {
                    update { existingTargetNames }
                }
            }
        Action(
            onClick = onClick,
            content = { Text(text) }
        )
    }

    @Composable
    private fun StartButton(
        contentPadding: PaddingValues,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(Dimens.largeSeparation),
            contentAlignment = Alignment.BottomEnd,
        ) {
            val start by model.start.collectAsState()
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.dictionaries_start)) },
                icon = { Icon { RocketLaunch } },
                onClick = { start?.invoke() },
                containerColor = when (start) {
                    null -> MaterialTheme.colorScheme.surface
                    else -> FloatingActionButtonDefaults.containerColor
                },
                expanded = !model.scrollState.canScrollBackward,
            )
        }
    }

    @Composable
    private fun Dictionaries(
        contentPadding: PaddingValues,
    ) {
        val currentUnselectedDictionaries: Set<DictionaryName> by model
            .unselectedNames
            .collectAsState()
        LazyColumn(
            state = model.scrollState,
            contentPadding = contentPadding + PaddingValues(bottom = 96.dp),
        ) {
            items(
                items = model.allDictionaryNames,
                key = DictionaryName::name,
            ) { name ->
                val selected = name !in currentUnselectedDictionaries
                Dictionary(
                    name = name,
                    selected = selected,
                    setIsSelected = remember(name, update) {
                        { newSelected ->
                            update { current ->
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
                    onClick = { model.edit(name) }
                ) {
                    Icon { Edit }
                }
            }
        )
    }
}