package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.shape.create

@JvmInline
value class TableCorners private constructor(
    private val packed: Byte,
) {

    constructor(
        startTopIsOpened: Boolean,
        startBottomIsOpened: Boolean,
        endTopIsOpened: Boolean,
        endBottomIsOpened: Boolean,
    ) : this(
        packed = ((if (startTopIsOpened) START_TOP else 0) +
                (if (startBottomIsOpened) START_BOTTOM else 0) +
                (if (endTopIsOpened) END_TOP else 0) +
                (if (endBottomIsOpened) END_BOTTOM else 0)).toByte()
    )

    val startTopIsOpened: Boolean
        get() = packed.toInt() and START_TOP != 0

    val startBottomIsOpened: Boolean
        get() = packed.toInt() and START_BOTTOM != 0

    val endTopIsOpened: Boolean
        get() = packed.toInt() and END_TOP != 0

    val endBottomIsOpened: Boolean
        get() = packed.toInt() and END_BOTTOM != 0

    private fun closePartially(
        closeStartTop: Boolean,
        closeStartBottom: Boolean,
        closeEndTop: Boolean,
        closeEndBottom: Boolean,
    ): TableCorners = TableCorners(
        startTopIsOpened = startTopIsOpened && !closeStartTop,
        startBottomIsOpened = startBottomIsOpened && !closeStartBottom,
        endTopIsOpened = endTopIsOpened && !closeEndTop,
        endBottomIsOpened = endBottomIsOpened && !closeEndBottom,
    )

    fun close(
        orientation: TableOrientation,
        startOrTop: Boolean,
        endOrBottom: Boolean,
    ): TableCorners = closePartially(
        closeStartTop = startOrTop,
        closeStartBottom = when (orientation) {
            TableOrientation.Vertical -> endOrBottom
            TableOrientation.Horizontal -> startOrTop
        },
        closeEndTop = when (orientation) {
            TableOrientation.Horizontal -> endOrBottom
            TableOrientation.Vertical -> startOrTop
        },
        closeEndBottom = endOrBottom,
    )

    @Composable
    fun toShape(): HnauShape = HnauShape.create(
        startTopRoundCorners = startTopIsOpened,
        startBottomRoundCorners = startBottomIsOpened,
        endTopRoundCorners = endTopIsOpened,
        endBottomRoundCorners = endBottomIsOpened,
    )

    companion object {

        private const val START_TOP = 0b0001
        private const val START_BOTTOM = 0b0010
        private const val END_TOP = 0b0100
        private const val END_BOTTOM = 0b1000

        val opened: TableCorners = TableCorners(
            startTopIsOpened = true,
            startBottomIsOpened = true,
            endTopIsOpened = true,
            endBottomIsOpened = true,
        )
    }
}