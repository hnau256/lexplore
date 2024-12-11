package hnau.lexplore.app

import hnau.common.app.storage.Storage
import hnau.common.kotlin.mapper.toMapper
import hnau.lexplore.data.api.dictionary.DictionaryRepository
import hnau.lexplore.model.init.api.InitModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.logging.Logger

class LexploreApp(
    scope: CoroutineScope,
    savedState: SavedState,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val storageFactory: Storage.Factory

        val dictionaryRepository: DictionaryRepository

        fun initStorage(): InitModel.Dependencies

        companion object
    }

    init {
        scope.launch {
            println("QWERTY. Collecting dictionaries")
            try {
                dependencies
                    .dictionaryRepository
                    .getDictionaries()
                    .dictionaries
                    .collect {
                        println("QWERTY. Dictionaries $it")
                    }
            } finally {
                println("QWERTY. End collecting dictionaries")
            }
        }
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
