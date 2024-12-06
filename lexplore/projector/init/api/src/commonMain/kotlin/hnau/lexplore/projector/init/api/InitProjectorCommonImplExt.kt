package hnau.lexplore.projector.init.api

import hnau.lexplore.projector.common.Localizer

expect fun InitProjector.Dependencies.Companion.commonImpl(
    localizer: Localizer,
): InitProjector.Dependencies