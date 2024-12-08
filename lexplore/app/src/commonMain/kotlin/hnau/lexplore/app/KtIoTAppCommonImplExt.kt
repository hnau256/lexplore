package hnau.lexplore.app

import hnau.common.app.storage.Storage

fun LexploreApp.Dependencies.Companion.commonImpl(
    storageFactory: Storage.Factory,
): LexploreApp.Dependencies = LexploreApp.Dependencies.impl(
    storageFactory = storageFactory,
)