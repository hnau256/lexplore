package hnau.lexplore.projector.common.backbutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.utils.Icon
import hnau.common.kotlin.coroutines.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BackButtonDelegate(
    private val scope: CoroutineScope,
    private val goBackHandler: GoBackHandler,
) : BackButtonWidthProvider {

    private val animatable: Animatable<Dp, AnimationVector1D> = Animatable(
        initialValue = 0.dp,
        typeConverter = Dp.VectorConverter
    )

    override val backButtonWidth: State<Dp>
        get() = animatable.asState()

    init {
        scope.launch {
            goBackHandler
                .mapState(scope) { goBackOrNull ->
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
    }

    @Composable
    fun Content() {
        val width: Dp by backButtonWidth
        Box(
            modifier = Modifier
                .size(BackButtonWidthProvider.maxBackButtonSize)
                .offset(x = width - BackButtonWidthProvider.maxBackButtonSize),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = { goBackHandler.value?.invoke() },
            ) {
                Icon { ArrowBack }
            }
        }
    }

}