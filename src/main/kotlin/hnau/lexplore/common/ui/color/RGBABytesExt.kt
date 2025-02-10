package hnau.lexplore.common.ui.color

import androidx.compose.ui.graphics.Color

val RGBABytes.compose: Color
    get() = Color(
        color = a.toLong().shl(24) +
                r.toLong().shl(16) +
                g.toLong().shl(8) +
                b.toLong().shl(0),
    )

fun RGBABytes.copy(
    r: UByte = this.r,
    g: UByte = this.g,
    b: UByte = this.b,
    a: UByte = this.a,
): RGBABytes = RGBABytes(
    r = r,
    g = g,
    b = b,
    a = a,
)

operator fun RGBABytes.get(
    index: Int,
): UByte = when (index) {
    0 -> r
    1 -> g
    2 -> b
    3 -> a
    else -> throw IndexOutOfBoundsException(index)
}

operator fun RGBABytes.component1(): UByte = r

operator fun RGBABytes.component2(): UByte = g

operator fun RGBABytes.component3(): UByte = b

operator fun RGBABytes.component4(): UByte = a