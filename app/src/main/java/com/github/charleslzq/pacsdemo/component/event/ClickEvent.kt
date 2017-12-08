package com.github.charleslzq.pacsdemo.component.event

/**
 * Created by charleslzq on 17-12-7.
 */
class ClickEvent {
    data class ChangeLayout(val layoutOrdinal: Int) : Event
    class TurnToMeasureLine: Event
    class TurnToMeasureAngle: Event
    data class ReverseColor(val layoutPosition: Int) : Event
    data class PseudoColor(val layoutPosition: Int) : Event
    data class ImageCellClicked(val layoutPosition: Int) : Event
    data class ThumbListItemClicked(val position: Int) : Event
}