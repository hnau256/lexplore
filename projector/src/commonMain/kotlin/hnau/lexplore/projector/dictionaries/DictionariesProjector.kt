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
        val items by model.items.collectAsState()
        val start by model.start.collectAsState()
        val selectAll by model.selectAll.collectAsState()
        val selectNone by model.selectNone.collectAsState()

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
                    actionOrElseOrDisabled = selectNone,
                    titleOrIcon = TitleOrIcon.Title(localization.none),
                )
                Action(
                    actionOrElseOrDisabled = selectAll,
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
                            items = items,
                            key = { item -> item.dictionaryName.name },
                        ) { item ->
                            SPanel(
                                actionOrElseOrDisabled = item.action,
                                importanceToActivate = null,
                            ) {
                                SItem(
                                    endAccessory = {
                                        SCheckBox(
                                            isChecked = item.isSelected,
                                        )
                                    },
                                    content = {
                                        SElements {
                                            SText(
                                                text = item.dictionaryName.name,
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
