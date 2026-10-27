package hnau.lexplore.app

import hnau.lexplore.model.RootModel
import org.hnau.commons.app.model.app.AppSeed
import hnau.lexplore.model.impl

fun createLEXploreAppSeed(
    dependencies: LEXploreAppDependencies,
): AppSeed<RootModel, RootModel.Skeleton> = AppSeed(
    skeletonSerializer = RootModel.Skeleton.serializer(),
    createDefaultSkeleton = { RootModel.Skeleton() },
    createModel = { scope, skeleton ->
        RootModel(
            scope = scope,
            dependencies = RootModel.Dependencies.impl(
                appDataFactory = dependencies.appDataFactory,
                dictionariesJson = dependencies.dictionariesJson,
                speaker = dependencies.speaker,
            ),
            skeleton = skeleton,
        )
    },
)
