package hnau.lexplore.app

import hnau.common.app.storage.Storage

expect fun LexploreApp.Dependencies.Companion.commonImpl(
    storageFactory: Storage.Factory,
): LexploreApp.Dependencies