package hnau.lexplore.ui.model.init

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.ui.color.buildColors
import hnau.lexplore.common.ui.color.material.MaterialHue
import hnau.lexplore.common.ui.uikit.Content
import hnau.lexplore.common.ui.uikit.backbutton.BackButtonDelegate
import hnau.lexplore.common.ui.uikit.backbutton.BackButtonWidthProvider
import hnau.lexplore.common.ui.uikit.bubble.BubblesShower
import hnau.lexplore.common.ui.uikit.bubble.Content
import hnau.lexplore.common.ui.uikit.bubble.SharedBubblesHolder
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.ui.model.mainstack.MainStackProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class InitProjector(
    scope: CoroutineScope,
    model: InitModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val context: Context

        fun mainStack(
            bubblesShower: BubblesShower,
            backButtonWidthProvider: BackButtonWidthProvider,
        ): MainStackProjector.Dependencies

        companion object
    }

    private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )


    private val backButtonDelegate: BackButtonDelegate = BackButtonDelegate(
        goBackHandler = model.goBackHandler,
    )

    private val mainSackProjector: StateFlow<Loadable<MainStackProjector>> = model
        .mainStackModel
        .mapWithScope(scope) { scope, mainStackOrLoading ->
            mainStackOrLoading.map { mainStack ->
                MainStackProjector(
                    scope = scope,
                    model = mainStack,
                    dependencies = dependencies.mainStack(
                        bubblesShower = bubblesHolder,
                        backButtonWidthProvider = backButtonDelegate,
                    )
                )
            }
        }

    @Composable
    fun Content() {
        MaterialTheme(
            colorScheme = buildColors(
                primaryHue = MaterialHue.Yellow,
                secondaryHue = MaterialHue.DeepPurple,
                tertiaryHue = MaterialHue.Pink,
            ),
            shapes = remember {
                val cornerSize = CornerSize(Dimens.cornerRadius)
                val shape = RoundedCornerShape(cornerSize)
                Shapes(
                    extraSmall = shape,
                    small = shape,
                    medium = shape,
                    large = shape,
                    extraLarge = shape,
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopStart,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onBackground,
                    LocalDensity provides Density(LocalDensity.current.density * 1.1f),
                ) {
                    mainSackProjector.Content { mainStackProjector ->
                        mainStackProjector.Content()
                    }
                    backButtonDelegate.Content()
                    bubblesHolder.Content()
                }
            }
        }
    }
}