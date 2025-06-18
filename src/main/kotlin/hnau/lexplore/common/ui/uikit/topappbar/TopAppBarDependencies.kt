package hnau.lexplore.common.ui.uikit.topappbar

import hnau.lexplore.common.ui.uikit.backbutton.BackButtonWidthProvider
import hnau.pipe.annotations.Pipe

@Pipe
interface TopAppBarDependencies {

    val backButtonWidthProvider: BackButtonWidthProvider
}