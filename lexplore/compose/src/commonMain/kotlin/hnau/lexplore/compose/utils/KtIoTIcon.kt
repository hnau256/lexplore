package hnau.lexplore.compose.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class KtIoTIconSize(
    val dp: Dp,
) {
    S32(32.dp),
    S64(64.dp),
    S128(128.dp),
    S256(256.dp),
    S512(512.dp),
}

object KtIoTIcon {

    private val cache = HashMap<KtIoTIconSize, ImageVector>()

    operator fun invoke(
        size: KtIoTIconSize,
    ): ImageVector = cache.getOrPut(size) {
        createKtIoTIcon(
            size = size.dp,
        )
    }

    val s32: ImageVector
        get() = invoke(KtIoTIconSize.S32)

    val s64: ImageVector
        get() = invoke(KtIoTIconSize.S64)

    val s128: ImageVector
        get() = invoke(KtIoTIconSize.S128)

    val s256: ImageVector
        get() = invoke(KtIoTIconSize.S256)

    val s512: ImageVector
        get() = invoke(KtIoTIconSize.S512)
}

private fun createKtIoTIcon(
    size: Dp,
): ImageVector = ImageVector
    .Builder(
        name = "KtIoTIcon",
        defaultWidth = size,
        defaultHeight = size,
        viewportWidth = 16f,
        viewportHeight = 16f,
    )
    .apply {
        path(
            name = "Background",
            fill = SolidColor(Color.White),
        ) {
            Icons.Filled.Circle
            moveTo(0f, 0f)
            horizontalLineTo(16f)
            verticalLineTo(16f)
            horizontalLineTo(0f)
            close()
        }
        group(
            rotate = 45f,
            translationX = 8f,
            translationY = 8f,
        ) {
            path(
                name = "KtIotSign",
                fill = Brush.radialGradient(
                    colors = listOf(
                        Color(0xffE54857),
                        Color(0xffC811E2),
                        Color(0xff7F52FF),
                    ),
                    center = Offset(-3f, 2f),
                    radius = 10f,
                ),
            ) {
                moveToRelative(-2.5f, -5.5f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, -1.0f)
                close()
                moveTo(-2.5f, -4.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(1.5f, -4.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(-4.5f, -2.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(-2.5f, -2.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(-0.5f, -2.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(1.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, -1.0f)
                close()
                moveTo(1.5f, -2.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(3.5f, -2.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(-4.5f, 1.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(-2.5f, 1.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(1.5f, 1.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(3.5f, 1.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(-2.5f, 3.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
                moveTo(1.5f, 3.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                close()
            }

            createShadow(
                left = -3.5f,
                top = -4.5f,
                horizontal = false,
                positive = true,
            )
            createShadow(
                left = 0.5f,
                top = -4.5f,
                horizontal = false,
                positive = true,
            )
            createShadow(
                left = 2.5f,
                top = -2.5f,
                horizontal = false,
                positive = true,
            )
            createShadow(
                left = 2.5f,
                top = 1.5f,
                horizontal = false,
                positive = true,
            )
            createShadow(
                left = -1.5f,
                top = 1.5f,
                horizontal = false,
                positive = true,
            )
            createShadow(
                left = -3.5f,
                top = -0.5f,
                horizontal = false,
                positive = true,
            )

            createShadow(
                left = -2.5f,
                top = -3.5f,
                horizontal = true,
                positive = true,
            )
            createShadow(
                left = 1.5f,
                top = -3.5f,
                horizontal = true,
                positive = true,
            )
            createShadow(
                left = 1.5f,
                top = 0.5f,
                horizontal = true,
                positive = true,
            )
            createShadow(
                left = -4.5f,
                top = 2.5f,
                horizontal = true,
                positive = true,
            )
            createShadow(
                left = -4.5f,
                top = -1.5f,
                horizontal = true,
                positive = true,
            )
            createShadow(
                left = -0.5f,
                top = 2.5f,
                horizontal = true,
                positive = true,
            )

            createShadow(
                left = -3.5f,
                top = -2.5f,
                horizontal = false,
                positive = false,
            )
            createShadow(
                left = 0.5f,
                top = -2.5f,
                horizontal = false,
                positive = false,
            )
            createShadow(
                left = 2.5f,
                top = -0.5f,
                horizontal = false,
                positive = false,
            )
            createShadow(
                left = 2.5f,
                top = 3.5f,
                horizontal = false,
                positive = false,
            )
            createShadow(
                left = -1.5f,
                top = 3.5f,
                horizontal = false,
                positive = false,
            )
            createShadow(
                left = -3.5f,
                top = 1.5f,
                horizontal = false,
                positive = false,
            )

            createShadow(
                left = -0.5f,
                top = -3.5f,
                horizontal = true,
                positive = false,
            )
            createShadow(
                left = 3.5f,
                top = -3.5f,
                horizontal = true,
                positive = false,
            )
            createShadow(
                left = 3.5f,
                top = 0.5f,
                horizontal = true,
                positive = false,
            )
            createShadow(
                left = 1.5f,
                top = 2.5f,
                horizontal = true,
                positive = false,
            )
            createShadow(
                left = -2.5f,
                top = 2.5f,
                horizontal = true,
                positive = false,
            )
            createShadow(
                left = -2.5f,
                top = -1.5f,
                horizontal = true,
                positive = false,
            )
        }
    }
    .build()

private fun ImageVector.Builder.createShadow(
    left: Float,
    top: Float,
    horizontal: Boolean,
    positive: Boolean,
) = path(
    fill = createGradient(
        left = left,
        top = top,
        horizontal = horizontal,
        positive = positive,
    ),
) {
    moveTo(left, top)
    horizontalLineToRelative(1f)
    verticalLineToRelative(1f)
    horizontalLineToRelative(-1f)
}

private fun createGradient(
    left: Float,
    top: Float,
    horizontal: Boolean,
    positive: Boolean,
): Brush {
    val (start, offset) = when (horizontal) {
        true -> when (positive) {
            true -> Pair(
                Offset(
                    x = left,
                    y = top + 0.5f,
                ),
                Offset(
                    x = 1f,
                    y = 0f,
                ),
            )

            false -> Pair(
                Offset(
                    x = left + 1,
                    y = top + 0.5f,
                ),
                Offset(
                    x = -1f,
                    y = 0f,
                ),
            )
        }

        false -> when (positive) {
            true -> Pair(
                Offset(
                    x = left + 0.5f,
                    y = top,
                ),
                Offset(
                    x = 0f,
                    y = 1f,
                ),
            )

            false -> Pair(
                Offset(
                    x = left + 0.5f,
                    y = top + 1,
                ),
                Offset(
                    x = 0f,
                    y = -1f,
                ),
            )
        }
    }
    val end = start + offset
    return Brush.linearGradient(
        0f to Color.Black.copy(alpha = 0f),
        0.5f to Color.Black.copy(alpha = 0.2f),
        1f to Color.Black.copy(alpha = 0.5f),
        start = start,
        end = end,
    )
}
