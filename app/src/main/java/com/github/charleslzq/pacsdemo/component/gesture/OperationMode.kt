package com.github.charleslzq.pacsdemo.component.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Created by charleslzq on 17-11-24.
 * 显示图像的ImageView在不同状态下有不同的触摸事件监听器, 对应不同的模式
 */
sealed class OperationMode(private vararg val listeners: (View, MotionEvent) -> Boolean) :
    View.OnTouchListener {
    override fun onTouch(view: View, motionEvent: MotionEvent) =
        listeners.any { it(view, motionEvent) }
}

/**
 * 播放模式, 默认模式
 */
class PlayMode(
    context: Context,
    playModeGestureListener: PlayModeGestureListener,
    private val gestureDetector: GestureDetector = GestureDetector(
        context,
        playModeGestureListener
    ),
    private val scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(
        context,
        playModeGestureListener
    )
) : OperationMode(
    { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) },
    { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) }
)

/**
 * 研究模式, 手势缩放值大于1时处于该模式.暂未使用
 */
class StudyMode(
    context: Context,
    studyModeGestureListener: StudyModeGestureListener,
    private val gestureDetector: GestureDetector = GestureDetector(
        context,
        studyModeGestureListener
    ),
    private val scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(
        context,
        studyModeGestureListener
    )
) : OperationMode(
    { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) },
    { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) }
)

/**
 * 测量模式, 提供测量线/角功能
 */
class MeasureMode(
    context: Context,
    measureModeGestureListener: MeasureModeGestureListener,
    private val gestureDetector: GestureDetector = GestureDetector(
        context,
        measureModeGestureListener
    ),
    private val scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(
        context,
        measureModeGestureListener
    )
) : OperationMode(
    measureModeGestureListener::onOtherGesture,
    { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) },
    { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) }
)