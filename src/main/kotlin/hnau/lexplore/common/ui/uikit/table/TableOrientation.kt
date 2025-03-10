package hnau.lexplore.common.ui.uikit.table

enum class TableOrientation {
    Vertical, Horizontal,;

    val opposite: TableOrientation
        get() = when (this) {
            Vertical -> Horizontal
            Horizontal -> Vertical
        }
}