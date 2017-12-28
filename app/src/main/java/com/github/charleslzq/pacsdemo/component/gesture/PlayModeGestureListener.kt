package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        dispatch: (Any) -> Unit
) : ScaleCompositeGestureListener(dispatch) {

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            dispatch(ImageFramesStore.IndexScroll(distanceX))
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {
//        dispatch(DragEventMessage.StartCopyCell(layoutPosition))
    }

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        dispatch(ImageFramesStore.PlayModeReset())
        return true
    }
}