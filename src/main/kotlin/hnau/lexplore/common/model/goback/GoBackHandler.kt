package hnau.lexplore.common.model.goback

import kotlinx.coroutines.flow.StateFlow

typealias GoBackHandler = StateFlow<(() -> Unit)?>
