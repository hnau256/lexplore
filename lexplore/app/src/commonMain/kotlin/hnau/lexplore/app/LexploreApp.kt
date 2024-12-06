package hnau.lexplore.app

import hnau.common.app.storage.Storage
import hnau.common.kotlin.mapper.toMapper
import hnau.lexplore.model.init.api.InitModel
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

        val storageFactory: Storage.Factory

        fun initStorage(): InitModel.Dependencies

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
            dependencies = dependencies.initStorage(),
        )

    val savableState: SavedState
        get() = modelSkeletonMapper.reverse(modelSkeleton).let(::SavedState)
}
