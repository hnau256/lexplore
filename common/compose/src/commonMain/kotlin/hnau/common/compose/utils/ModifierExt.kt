package hnau.common.compose.utils

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role


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