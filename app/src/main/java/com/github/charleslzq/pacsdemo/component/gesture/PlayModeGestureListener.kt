package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.ColorMatrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        framesStore: ImageFramesStore
) : ScaleCompositeGestureListener(framesStore) {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        if (framesStore.playable()) {
            framesStore.playing = !framesStore.playing
        }
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            val rawDistance = (distanceX / 10).toInt()
            framesStore.currentIndex = Math.min(Math.max(framesStore.currentIndex - rawDistance, 0), framesStore.framesModel.size - 1)
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        EventBus.post(DragEventMessage.StartCopyCell(framesStore.layoutPosition))
    }

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        if (framesStore.playable()) {
            framesStore.playing = false
        }
        framesStore.colorMatrix = ColorMatrix()
        framesStore.currentIndex = 0
        framesStore.pseudoColor = false
        return true
    }
}