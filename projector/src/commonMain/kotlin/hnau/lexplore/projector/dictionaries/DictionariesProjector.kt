package hnau.lexplore.projector.dictionaries

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.lexplore.model.dictionaries.DictionariesModel
import hnau.lexplore.projector.Localization
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SCheckBox
import org.hnau.commons.app.projector.fractal.SContentWithActions
import org.hnau.commons.app.projector.fractal.SElements
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.ifTrue

class DictionariesProjector(
    private val model: DictionariesModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val localization = dependencies.localization
        val unselectedNames by model.unselectedNames.collectAsState()
        val start by model.start.collectAsState()
        val startAction = (unselectedNames.size < model.allDictionaryNamesSet.size)
            .ifTrue { start }
        val selectAllAction: (() -> Unit)? = unselectedNames
            .isNotEmpty()
            .ifTrue { { model.update { emptySet() } } }
        val selectNoneAction: (() -> Unit)? = (unselectedNames.size != model.allDictionaryNamesSet.size)
            .ifTrue { { model.update { model.allDictionaryNamesSet } } }

        SScreen(
            contentPadding = contentPadding,
            title = {
                SText(
                    text = localization.dictionariesTitle,
                    type = SizeType.Large,
                )
            },
            actions = {
                Action(
                    actionOrElseOrDisabled = ActionOrElse.instant(selectNoneAction),
                    titleOrIcon = TitleOrIcon.Title(localization.none),
                )
                Action(
                    actionOrElseOrDisabled = ActionOrElse.instant(selectAllAction),
                    titleOrIcon = TitleOrIcon.Title(localization.all),
                )
            },
        ) {
            SContentWithActions(
                content = {
                    SLazyTable(
                        orientation = Orientation.Vertical,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        cells(
                            items = model.items,
                            key = { item -> item.dictionaryName.name },
                        ) { item ->
                            val dictionaryName = item.dictionaryName
                            val isSelected = dictionaryName !in unselectedNames
                            SPanel(
                                actionOrElseOrDisabled = ActionOrElse.instant {
                                    model.update { current ->
                                        isSelected.foldBoolean(
                                            ifTrue = { current + dictionaryName },
                                            ifFalse = { current - dictionaryName },
                                        )
                                    }
                                },
                                importanceToActivate = null,
                            ) {
                                SItem(
                                    endAccessory = {
                                        SCheckBox(
                                            isChecked = isSelected,
                                        )
                                    },
                                    content = {
                                        SElements {
                                            SText(
                                                text = dictionaryName.name,
                                                type = SizeType.Medium,
                                            )
                                            SText(
                                                text = localization.dictionarySubtitle(
                                                    item.knownWordsCount,
                                                    item.totalWordsCount - item.knownWordsCount,
                                                    item.totalWordsCount,
                                                ),
                                                type = SizeType.Small,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
                actions = {
                    SButton(
                        actionOrElseOrDisabled = startAction,
                        titleOrIcon = TitleOrIcon.Both(
                            title = localization.start,
                            icon = Drawable.Vector(Icons.Default.RocketLaunch),
                        ),
                    )
                },
            )
        }
    }
}
