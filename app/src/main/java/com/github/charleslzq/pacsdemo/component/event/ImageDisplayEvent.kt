package com.github.charleslzq.pacsdemo.component.event

import android.graphics.PointF

/**
 * Created by charleslzq on 17-12-8.
 */
class ImageDisplayEvent {
    class ScaleChange(layoutPosition: Int, val scaleFactor: Float) : ImageCellEvent(layoutPosition)
    class ChangePlayStatus(layoutPosition: Int) : ImageCellEvent(layoutPosition)
    class PlayModeReset(layoutPosition: Int) : ImageCellEvent(layoutPosition)
    class IndexChange(layoutPosition: Int, val index: Int) : ImageCellEvent(layoutPosition)
    class IndexScroll(layoutPosition: Int, val scroll: Int) : ImageCellEvent(layoutPosition)
    class LocationTranslate(layoutPosition: Int, val distanceX: Float, val distanceY: Float) : ImageCellEvent(layoutPosition)
    class StudyModeReset(layoutPosition: Int) : ImageCellEvent(layoutPosition)
    class DrawPath(layoutPosition: Int, val points: List<PointF>) : ImageCellEvent(layoutPosition)
    class AddPath(layoutPosition: Int, val points: List<PointF>, val text: Pair<PointF, String>) : ImageCellEvent(layoutPosition)
    class MeasureModeReset(layoutPosition: Int) : ImageCellEvent(layoutPosition)
}