package com.github.charleslzq.pacsdemo.component.event

/**
 * Created by charleslzq on 17-12-8.
 */
class ImageDisplayEvent {
    data class ScaleChange(val layoutPosition: Int, val scaleFactor: Float) : Event
    data class ChangePlayStatus(val layoutPosition: Int) : Event
    data class PlayModeReset(val layoutPosition: Int) : Event
    data class IndexChange(val layoutPosition: Int, val index: Int) : Event
    data class IndexScroll(val layoutPosition: Int, val scroll: Int) : Event
}