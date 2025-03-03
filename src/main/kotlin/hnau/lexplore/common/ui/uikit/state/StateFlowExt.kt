package hnau.lexplore.common.ui.uikit.state

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.StateContent(
    modifier: Modifier = Modifier,
    label: String,
    contentKey: (targetState: T) -> Any?,
    content: @Composable AnimatedContentScope.(targetState: T) -> Unit,
) {
    val value by collectAsState()
    AnimatedContent(
        targetState = value,
        contentKey = contentKey,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        label = label,
        content = content,
    )
}