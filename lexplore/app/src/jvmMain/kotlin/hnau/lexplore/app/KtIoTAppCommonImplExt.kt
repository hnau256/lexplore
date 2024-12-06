package hnau.lexplore.app

import hnau.common.app.storage.Storage

actual fun LexploreApp.Dependencies.Companion.commonImpl(
    storageFactory: Storage.Factory,
): LexploreApp.Dependencies = LexploreApp.Dependencies.impl(
    storageFactory = storageFactory,
)