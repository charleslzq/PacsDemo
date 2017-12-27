package com.github.charleslzq.pacsdemo.component.event

/**
 * Created by charleslzq on 17-12-7.
 */
class ClickEvent {
    data class ChangeLayout(val layoutOrdinal: Int)
    data class ImageClicked(override val layoutPosition: Int) : ImageCellEvent()
    data class ImageContextClicked(override val layoutPosition: Int) : ImageCellEvent()
    data class TurnToMeasureLine(override val layoutPosition: Int) : ImageCellEvent()
    data class TurnToMeasureAngle(override val layoutPosition: Int) : ImageCellEvent()
    data class ReverseColor(override val layoutPosition: Int) : ImageCellEvent()
    data class PseudoColor(override val layoutPosition: Int) : ImageCellEvent()
    data class ThumbListItemClicked(val position: Int)
}