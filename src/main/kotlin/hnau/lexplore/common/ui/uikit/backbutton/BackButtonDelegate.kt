package hnau.lexplore.common.ui.uikit.backbutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.model.goback.GoBackHandler
import hnau.lexplore.common.ui.utils.Icon
import kotlinx.coroutines.flow.collectLatest

class BackButtonDelegate(
    private val goBackHandler: GoBackHandler,
) : BackButtonWidthProvider {

    private val animatable: Animatable<Dp, AnimationVector1D> = Animatable(
        initialValue = 0.dp,
        typeConverter = Dp.VectorConverter
    )

    override val backButtonWidth: State<Dp>
        get() = animatable.asState()

    @Composable
    fun Content() {
        LaunchedEffect(goBackHandler, animatable) {
            goBackHandler
                .mapState(this) { goBackOrNull ->
                    when (goBackOrNull) {
                        null -> 0.dp
                        else -> BackButtonWidthProvider.maxBackButtonSize
                    }
                }
                .collectLatest { width ->
                    animatable.animateTo(
                        targetValue = width,
                        animationSpec = TweenSpec(),
                    )
                }
        }
        val width: Dp by backButtonWidth
        Box(
            modifier = Modifier
                .systemBarsPadding()
                .size(BackButtonWidthProvider.maxBackButtonSize)
                .offset(x = width - BackButtonWidthProvider.maxBackButtonSize),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = { goBackHandler.value?.invoke() },
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                ) { ArrowBack }
            }
        }
    }

}