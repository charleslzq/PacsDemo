package com.github.charleslzq.pacsdemo.component.event

/**
 * Created by charleslzq on 17-12-7.
 */
class ClickEvent {
    data class ChangeLayout(val layoutOrdinal: Int)
    class TurnToMeasureLine
    class TurnToMeasureAngle
    class ReverseColor(layoutPosition: Int) : ImageCellEvent(layoutPosition)
    class PseudoColor(layoutPosition: Int) : ImageCellEvent(layoutPosition)
    class ImageCellClicked(layoutPosition: Int) : ImageCellEvent(layoutPosition)
    class ThumbListItemClicked(val position: Int)
}