package hnau.lexplore.projector.init.api

import hnau.lexplore.projector.common.Localizer

fun InitProjector.Dependencies.Companion.commonImpl(
    localizer: Localizer,
): InitProjector.Dependencies = InitProjector.Dependencies.impl(
    localizer = localizer,
)