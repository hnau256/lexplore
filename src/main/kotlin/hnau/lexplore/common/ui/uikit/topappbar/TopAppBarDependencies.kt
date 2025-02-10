package hnau.lexplore.common.ui.uikit.topappbar

import hnau.lexplore.common.ui.uikit.backbutton.BackButtonWidthProvider
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface TopAppBarDependencies {

    val backButtonWidthProvider: BackButtonWidthProvider
}