package hnau.common.compose.uikit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.AnimatedNullableVisibility
import hnau.common.compose.utils.AnimatedVisibilityTransitions

@Composable
fun ErrorPanel(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    button: (@Composable () -> Unit)? = null,
) = Column(
    modifier = modifier
        .padding(horizontal = Dimens.largeSeparation)
        .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(
        Dimens.largeSeparation,
        Alignment.CenterVertically,
    ),
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.h6,
    ) {
        title()
    }
    AnimatedNullableVisibility(
        value = button,
        transitions = AnimatedVisibilityTransitions.vertical,
    ) { buttonLocal ->
        buttonLocal()
    }
}
