package hnau.lexplore.common.ui.uikit.color

import androidx.compose.ui.graphics.Color
import hnau.lexplore.common.ui.color.RGBABytes


val RGBABytes.compose: Color
    get() = Color(
        color = a.toLong().shl(24) +
                r.toLong().shl(16) +
                g.toLong().shl(8) +
                b.toLong().shl(0),
    )