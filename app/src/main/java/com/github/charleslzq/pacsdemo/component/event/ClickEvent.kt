package com.github.charleslzq.pacsdemo.component.event

/**
 * Created by charleslzq on 17-12-7.
 */
class ClickEvent {
    data class ChangeLayout(val layoutOrdinal: Int)
    data class ImageClicked(override val layoutPosition: Int) : ImageCellEvent()
    data class MeasureLineTurned(override val layoutPosition: Int, val isSelected: Boolean) : ImageCellEvent()
    data class MeasureAngleTurned(override val layoutPosition: Int, val isSelected: Boolean) : ImageCellEvent()
    data class ReverseColor(override val layoutPosition: Int) : ImageCellEvent()
    data class PseudoColor(override val layoutPosition: Int) : ImageCellEvent()
    data class Undo(override val layoutPosition: Int) : ImageCellEvent()
    data class Redo(override val layoutPosition: Int) : ImageCellEvent()
    data class ThumbListItemClicked(val position: Int)
}