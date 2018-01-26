package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.store.ImageDisplayActions

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(dispatch: (Any) -> Unit, private val onDragStart: () -> Unit) :
    ScaleCompositeGestureListener(dispatch) {

    /**
     * 左右滑动选择一个series中的指定图片
     */
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ) = if (e1?.pointerCount == 1 && e2?.pointerCount == 1) {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            dispatch(ImageDisplayActions.indexScroll(distanceX))
        }
        true
    } else {
        false
    }


    /**
     * 响应托放操作
     */
    override fun onLongPress(e: MotionEvent) = onDragStart()

    /**
     * 双击重置
     */
    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        dispatch(ImageDisplayActions.resetDisplay())
        return true
    }
}