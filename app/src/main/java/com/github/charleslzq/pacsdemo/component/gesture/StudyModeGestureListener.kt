package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(dispatch: (Any) -> Unit, private val onDragStart: () -> Unit) :
    ScaleCompositeGestureListener(dispatch) {

    /**
     * 双击重置
     */
    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        dispatch(ImageFrameStore.StudyModeReset())
        return true
    }

    /**
     * 左右滑动移动图像位置
     */
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ) = if (e1?.pointerCount == 1 && e2?.pointerCount == 1) {
        dispatch(ImageFrameStore.LocationTranslate(distanceX, distanceY))
        true
    } else {
        false
    }

    /**
     * 响应托放操作
     */
    override fun onLongPress(e: MotionEvent) = onDragStart()
}