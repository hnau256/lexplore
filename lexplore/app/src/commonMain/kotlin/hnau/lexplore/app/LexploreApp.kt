package hnau.lexplore.app

import hnau.common.kotlin.mapper.toMapper
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.prefiller.api.PrefillDataProvider
import hnau.lexplore.prefiller.api.Prefiller
import hnau.lexplore.prefiller.impl.PrefillerImpl
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

class LexploreApp(
    scope: CoroutineScope,
    savedState: SavedState,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun init(
            prefiller: Prefiller,
        ): InitModel.Dependencies

        val prefillDataProvider: PrefillDataProvider

        companion object
    }

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val modelSkeletonMapper =
        json.toMapper(InitModel.Skeleton.serializer())

    private val modelSkeleton = savedState
        .savedState
        ?.let(modelSkeletonMapper.direct)
        ?: InitModel.Skeleton()

    val model = InitModel
        .Factory
        .assemble()
        .createInitModel(
            scope = scope,
            skeleton = modelSkeleton,
            dependencies = dependencies.init(
                prefiller = PrefillerImpl(
                    prefillDataProvider = dependencies.prefillDataProvider,
                )
            ),
        )

    val savableState: SavedState
        get() = modelSkeletonMapper.reverse(modelSkeleton).let(::SavedState)
}
