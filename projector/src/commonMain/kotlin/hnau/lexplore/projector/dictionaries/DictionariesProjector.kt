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
import org.hnau.commons.app.projector.fractal.table.STableHeader
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.fractal.table.lazy.item
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant

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
                    actionOrElseOrDisabled = ActionOrElse.instant {
                        model.update { model.allDictionaryNamesSet }
                    },
                    titleOrIcon = TitleOrIcon.Title(localization.none),
                )
                Action(
                    actionOrElseOrDisabled = ActionOrElse.instant {
                        model.update { emptySet() }
                    },
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
                        item {
                            STableHeader {
                                SText(localization.dictionariesTitle)
                            }
                        }
                        cells(
                            items = model.items,
                            key = { item -> item.dictionaryName.name },
                        ) { item ->
                            val dictionaryName = item.dictionaryName
                            val isSelected = dictionaryName !in unselectedNames
                            SPanel {
                                SItem(
                                    startAccessory = {
                                        SCheckBox(
                                            isChecked = isSelected,
                                            onClick = {
                                                model.update { current ->
                                                    if (isSelected) {
                                                        current + dictionaryName
                                                    } else {
                                                        current - dictionaryName
                                                    }
                                                }
                                            },
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
                        actionOrElseOrDisabled = start,
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
