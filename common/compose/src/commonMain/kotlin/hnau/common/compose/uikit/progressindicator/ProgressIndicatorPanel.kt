package hnau.common.compose.uikit.progressindicator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.Space
import hnau.common.compose.uikit.utils.Dimens

@Composable
fun ProgressIndicatorPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = Column(
    modifier = modifier
        .fillMaxSize()
        .padding(Dimens.separation),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    ProgressIndicator(
        size = ProgressIndicatorSize.large,
    )
    Space()
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colors.onBackground,
        LocalTextStyle provides MaterialTheme.typography.subtitle1,
    ) {
        content()
    }
}
