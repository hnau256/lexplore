package hnau.lexplore.app

import hnau.lexplore.common.AppDataFactory
import hnau.lexplore.common.speaker.Speaker
import org.hnau.commons.gen.pipe.annotations.Pipe

@Pipe
interface LEXploreAppDependencies {

    val speaker: Speaker

    val dictionariesJson: String

    val appDataFactory: AppDataFactory

    companion object
}
