package com.github.charleslzq.pacsdemo.component.event

/**
 * Created by charleslzq on 17-12-1.
 */
class DragEvent {
    data class StartAtCell (
            val layoutPosition: Int,
            val dataPosition: Int
    )
}