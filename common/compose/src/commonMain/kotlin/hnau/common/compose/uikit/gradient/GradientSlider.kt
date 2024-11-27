package hnau.common.compose.uikit.gradient

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import hnau.common.color.gradient.Gradient
import hnau.common.color.gradient.export
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    gradient: Gradient<Color>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val onValueChangeState = rememberUpdatedState(onValueChange)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val focusRequester = remember { FocusRequester() }
    BoxWithConstraints(
        modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = ThumbRadius * 2,
                minHeight = ThumbRadius * 2,
            )
            .sliderSemantics(
                value = value,
                enabled = enabled,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
            )
            .focusRequester(focusRequester)
            .focusable(
                enabled = enabled,
                interactionSource = interactionSource,
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val maxPx: Float
        val minPx: Float

        with(LocalDensity.current) {
            maxPx = max(widthPx - ThumbRadius.toPx(), 0f)
            minPx = min(ThumbRadius.toPx(), maxPx)
        }

        fun scaleToUserValue(offset: Float) =
            scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

        fun scaleToOffset(userValue: Float) =
            scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

        val scope = rememberCoroutineScope()
        val rawOffset = remember { mutableFloatStateOf(scaleToOffset(value)) }
        val pressOffset = remember { mutableFloatStateOf(0f) }

        val draggableState = remember(minPx, maxPx, valueRange) {
            SliderDraggableState {
                rawOffset.floatValue = (rawOffset.floatValue + it + pressOffset.floatValue)
                pressOffset.floatValue = 0f
                val offsetInTrack = rawOffset.floatValue.coerceIn(minPx, maxPx)
                onValueChangeState.value.invoke(scaleToUserValue(offsetInTrack))
            }
        }

        CorrectValueSideEffect(::scaleToOffset, valueRange, minPx..maxPx, rawOffset, value)

        val gestureEndAction = rememberUpdatedState<(Float) -> Unit> { velocity: Float ->
            val current = rawOffset.floatValue
            focusRequester.requestFocus()
            if (current != current) {
                scope.launch {
                    animateToTarget(draggableState, current, current, velocity)
                    onValueChangeFinished?.invoke()
                }
            } else if (!draggableState.isDragging) {
                // check ifDragging in case the change is still in progress (touch -> drag case)
                onValueChangeFinished?.invoke()
            }
        }
        val press = Modifier.sliderTapModifier(
            draggableState = draggableState,
            interactionSource = interactionSource,
            maxPx = widthPx,
            isRtl = isRtl,
            rawOffset = rawOffset,
            gestureEndAction = gestureEndAction,
            pressOffset = pressOffset,
            enabled = enabled
        )

        val drag = Modifier.draggable(
            orientation = Orientation.Horizontal,
            reverseDirection = isRtl,
            enabled = enabled,
            interactionSource = interactionSource,
            onDragStopped = { velocity -> gestureEndAction.value.invoke(velocity) },
            startDragImmediately = draggableState.isDragging,
            state = draggableState
        )

        val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
        val fraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)
        SliderImpl(
            enabled = enabled,
            positionFraction = fraction,
            width = maxPx - minPx,
            interactionSource = interactionSource,
            gradient = gradient,
            modifier = press.then(drag)
        )
    }
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    gradient: Gradient<Color>,
    positionFraction: Float,
    width: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier,
) {
    Box(modifier.then(DefaultSliderConstraints)) {
        val thumbPx: Float
        val widthDp: Dp
        with(LocalDensity.current) {
            thumbPx = ThumbRadius.toPx()
            widthDp = width.toDp()
        }

        val thumbSize = ThumbRadius * 2
        val offset = widthDp * positionFraction

        val colors: GradientColors = remember(gradient) { GradientColors(gradient) }

        Track(
            modifier = Modifier.fillMaxSize(),
            enabled = enabled,
            positionFractionEnd = positionFraction,
            thumbPx = thumbPx,
            colors = colors,
        )
        SliderThumb(
            modifier = Modifier,
            offset = offset,
            interactionSource = interactionSource,
            enabled = enabled,
            thumbSize = thumbSize,
            fraction = positionFraction,
            colors = colors,
        )
    }
}

@Immutable
private class GradientColors(
    val colors: Array<Pair<Float, Color>>,
) {

    constructor(
        gradient: Gradient<Color>,
    ) : this(
        colors = gradient.export(),
    )

    operator fun get(
        fraction: Float,
    ): Color {
        repeat(colors.size - 1) { stopIndex ->
            val (afterFraction, afterColor) = colors[stopIndex + 1]
            val afterDelta = afterFraction - fraction
            if (afterDelta >= 0) {
                val (beforeFraction, beforeColor) = colors[stopIndex]
                val beforeDelta = fraction - beforeFraction
                val beforeToAfter = (afterFraction - beforeFraction)
                    .takeIf { it > 0 }
                    ?: return beforeColor
                val beforeToAfterFraction = beforeDelta / beforeToAfter
                return lerp(
                    start = beforeColor,
                    stop = afterColor,
                    fraction = beforeToAfterFraction,
                )
            }
        }
        return colors.last().second
    }
}

@Composable
private fun BoxScope.SliderThumb(
    modifier: Modifier,
    offset: Dp,
    fraction: Float,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    thumbSize: Dp,
    colors: GradientColors,
) {
    Box(
        Modifier
            .padding(start = offset)
            .align(Alignment.CenterStart)
    ) {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }

        val elevation = when {
            !enabled -> 0.dp
            interactions.isNotEmpty() -> ThumbPressedElevation
            else -> ThumbDefaultElevation
        }
        Spacer(
            modifier
                .size(thumbSize, thumbSize)
                .then(
                    when (enabled) {
                        false -> Modifier
                        true -> Modifier
                            .indication(
                                interactionSource = interactionSource,
                                indication = rememberRipple(
                                    bounded = false,
                                    radius = ThumbRippleRadius
                                )
                            )
                            .hoverable(interactionSource = interactionSource)
                            .shadow(elevation, CircleShape, clip = false)
                    }
                )
                .background(
                    color = colors[fraction],
                    shape = CircleShape,
                )
        )
    }
}

@Composable
private fun Track(
    modifier: Modifier,
    colors: GradientColors,
    enabled: Boolean,
    positionFractionEnd: Float,
    thumbPx: Float,
) {
    val density = LocalDensity.current
    val afterHeightPx = with(density) { InactiveTrackHeight.toPx() }
    val beforeHeight by animateDpAsState(
        when (enabled) {
            true -> ActiveTrackHeight
            false -> InactiveTrackHeight
        }
    )
    val beforeHeightPx = with(density) { beforeHeight.toPx() }
    Canvas(modifier) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(thumbPx, center.y)
        val sliderRight = Offset(size.width - thumbPx, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight

        val gradient = Brush.linearGradient(
            colorStops = colors.colors,
            start = sliderLeft,
            end = sliderRight,
        )

        val sliderValue = Offset(
            x = sliderStart.x + (sliderEnd.x - sliderStart.x) * positionFractionEnd,
            y = center.y
        )

        drawLine(
            brush = gradient,
            start = sliderStart,
            end = sliderValue,
            strokeWidth = beforeHeightPx,
            cap = StrokeCap.Round,
        )

        drawLine(
            brush = gradient,
            start = sliderValue,
            end = sliderEnd,
            strokeWidth = afterHeightPx,
            cap = StrokeCap.Round,
        )
    }
}

private fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

private fun lerp(min: Float, max: Float, fraction: Float): Float =
    min + (max - min) * fraction

private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

@Composable
private fun CorrectValueSideEffect(
    scaleToOffset: (Float) -> Float,
    valueRange: ClosedFloatingPointRange<Float>,
    trackRange: ClosedFloatingPointRange<Float>,
    valueState: MutableState<Float>,
    value: Float,
) {
    SideEffect {
        val error = (valueRange.endInclusive - valueRange.start) / 1000
        val newOffset = scaleToOffset(value)
        if (abs(newOffset - valueState.value) > error) {
            if (valueState.value in trackRange) {
                valueState.value = newOffset
            }
        }
    }
}

private fun Modifier.sliderSemantics(
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                val newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (newValue == coerced) {
                    false
                } else {
                    onValueChange(newValue)
                    onValueChangeFinished?.invoke()
                    true
                }
            }
        )
    }.progressSemantics(
        value = value,
        valueRange = valueRange,
        steps = 0,
    )
}

private fun Modifier.sliderTapModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Float,
    isRtl: Boolean,
    rawOffset: State<Float>,
    gestureEndAction: State<(Float) -> Unit>,
    pressOffset: MutableState<Float>,
    enabled: Boolean,
) = composed(
    factory = {
        if (enabled) {
            val scope = rememberCoroutineScope()
            pointerInput(draggableState, interactionSource, maxPx, isRtl) {
                detectTapGestures(
                    onPress = { pos ->
                        val to = if (isRtl) maxPx - pos.x else pos.x
                        pressOffset.value = to - rawOffset.value
                        try {
                            awaitRelease()
                        } catch (_: GestureCancellationException) {
                            pressOffset.value = 0f
                        }
                    },
                    onTap = {
                        scope.launch {
                            draggableState.drag(MutatePriority.UserInput) {
                                // just trigger animation, press offset will be applied
                                dragBy(0f)
                            }
                            gestureEndAction.value.invoke(0f)
                        }
                    }
                )
            }
        } else {
            this
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "sliderTapModifier"
        properties["draggableState"] = draggableState
        properties["interactionSource"] = interactionSource
        properties["maxPx"] = maxPx
        properties["isRtl"] = isRtl
        properties["rawOffset"] = rawOffset
        properties["gestureEndAction"] = gestureEndAction
        properties["pressOffset"] = pressOffset
        properties["enabled"] = enabled
    })

private suspend fun animateToTarget(
    draggableState: DraggableState,
    current: Float,
    target: Float,
    velocity: Float,
) {
    draggableState.drag {
        var latestValue = current
        Animatable(initialValue = current).animateTo(target, SliderToTickAnimation, velocity) {
            dragBy(this.value - latestValue)
            latestValue = this.value
        }
    }
}

// Internal to be referred to in tests
internal val ThumbRadius = 8.dp
private val ThumbRippleRadius = 24.dp
private val ThumbDefaultElevation = 4.dp
private val ThumbPressedElevation = 8.dp

// Internal to be referred to in tests
internal val ActiveTrackHeight = 6.dp
internal val InactiveTrackHeight = 4.dp
private val SliderHeight = 48.dp
private val SliderMinWidth = 144.dp // TODO: clarify min width
private val DefaultSliderConstraints =
    Modifier
        .widthIn(min = SliderMinWidth)
        .heightIn(max = SliderHeight)

private val SliderToTickAnimation = TweenSpec<Float>(durationMillis = 100)

private class SliderDraggableState(
    val onDelta: (Float) -> Unit,
) : DraggableState {

    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float): Unit = onDelta(pixels)
    }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit,
    ): Unit = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }

    override fun dispatchRawDelta(delta: Float) {
        return onDelta(delta)
    }
}
