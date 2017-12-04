package com.github.charleslzq.pacsdemo.component.event

/**
 * Created by charleslzq on 17-12-1.
 */
class DragEventMessage {
    data class StartCopyCell(
            val layoutPosition: Int
    )

    data class DropAtCellWithData(
            val layoutPosition: Int,
            val dataPosition: Int
    )

    data class DropToCopyCell(
            val sourcePosition: Int,
            val targetPosition: Int
    )
}