package com.github.charleslzq.pacsdemo.component.event

import android.graphics.PointF

/**
 * Created by charleslzq on 17-12-8.
 */
class ImageDisplayEvent {
    data class ScaleChange(val layoutPosition: Int, val scaleFactor: Float) : Event
    data class ChangePlayStatus(val layoutPosition: Int) : Event
    data class PlayModeReset(val layoutPosition: Int) : Event
    data class IndexChange(val layoutPosition: Int, val index: Int) : Event
    data class IndexScroll(val layoutPosition: Int, val scroll: Int) : Event
    data class LocationTranslate(val layoutPosition: Int, val distanceX: Float, val distanceY: Float) : Event
    data class StudyModeReset(val layoutPosition: Int) : Event
    data class DrawPath(val layoutPosition: Int, val points: List<PointF>) : Event
    data class AddPath(val layoutPosition: Int, val points: List<PointF>, val text: Pair<PointF, String>) : Event
    data class MeasureModeReset(val layoutPosition: Int) : Event
}