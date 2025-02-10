package hnau.lexplore.common.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import hnau.lexplore.common.ui.uikit.utils.Dimens


@Composable
fun Modifier.option(
    buildModifierOrNull: @Composable Modifier.() -> Modifier?,
): Modifier = buildModifierOrNull()
    ?.let(Modifier::then)
    ?: this

fun Modifier.clickableOption(
    onClick: (() -> Unit)?,
    onClickLabel: String? = null,
    role: Role? = null,
): Modifier = clickable(
    enabled = onClick != null,
    onClickLabel = onClickLabel,
    role = role,
    onClick = { onClick?.invoke() },
)

fun Modifier.horizontalDisplayPadding(): Modifier = padding(
    horizontal = Dimens.separation,
)

fun Modifier.verticalDisplayPadding(): Modifier = padding(
    vertical = Dimens.separation,
)