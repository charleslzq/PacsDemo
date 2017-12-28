package com.github.charleslzq.pacsdemo.component.event

import android.graphics.PointF

/**
 * Created by charleslzq on 17-12-8.
 */
class ImageDisplayEvent {
    data class ScaleChange(override val layoutPosition: Int, val scaleFactor: Float) : ImageCellEvent()
    data class ChangePlayStatus(override val layoutPosition: Int) : ImageCellEvent()
    data class PlayModeReset(override val layoutPosition: Int) : ImageCellEvent()
    data class IndexChange(override val layoutPosition: Int, val index: Int, val fromUser: Boolean) : ImageCellEvent()
    data class IndexScroll(override val layoutPosition: Int, val scroll: Float) : ImageCellEvent()
    data class LocationTranslate(override val layoutPosition: Int, val distanceX: Float, val distanceY: Float) : ImageCellEvent()
    data class StudyModeReset(override val layoutPosition: Int) : ImageCellEvent()
    data class AddPath(override val layoutPosition: Int, val points: List<PointF>, val text: Pair<PointF, String>) : ImageCellEvent()
    data class DrawLines(override val layoutPosition: Int, val points: List<PointF>) : ImageCellEvent()
}