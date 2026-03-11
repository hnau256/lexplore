package hnau.lexplore.common.ui.uikit.topappbar

import hnau.lexplore.common.ui.uikit.backbutton.BackButtonWidthProvider
import org.hnau.commons.gen.pipe.annotations.Pipe

@Pipe
interface TopAppBarDependencies {

    val backButtonWidthProvider: BackButtonWidthProvider
}