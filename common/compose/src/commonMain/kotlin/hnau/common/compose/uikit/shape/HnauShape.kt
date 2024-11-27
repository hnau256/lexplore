package hnau.common.compose.uikit.shape

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import hnau.common.compose.uikit.utils.Dimens

@Immutable
class HnauShape(
    private val startTopRadiusFraction: Float = 1f,
    private val endTopRadiusFraction: Float = 1f,
    private val startBottomRadiusFraction: Float = 1f,
    private val endBottomRadiusFraction: Float = 1f,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val maxRadius = with(density) { Dimens.cornerRadius.toPx() }

        val leftTopRadius = CornerRadius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> startTopRadiusFraction
                LayoutDirection.Rtl -> endTopRadiusFraction
            } * maxRadius,
        )

        val rightTopRadius = CornerRadius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> endTopRadiusFraction
                LayoutDirection.Rtl -> startTopRadiusFraction
            } * maxRadius,
        )

        val leftBottomRadius = CornerRadius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> startBottomRadiusFraction
                LayoutDirection.Rtl -> endBottomRadiusFraction
            } * maxRadius,
        )

        val rightBottomRadius = CornerRadius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> endBottomRadiusFraction
                LayoutDirection.Rtl -> startBottomRadiusFraction
            } * maxRadius,
        )

        return Outline.Rounded(
            RoundRect(
                rect = size.toRect(),
                topLeft = leftTopRadius,
                topRight = rightTopRadius,
                bottomLeft = leftBottomRadius,
                bottomRight = rightBottomRadius,
            ),
        )
    }

    companion object {

        val default = HnauShape()
    }
}
