package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.github.charleslzq.pacsdemo.component.store.ImageActions
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore
import java.util.*

/**
 * Created by charleslzq on 17-11-30.
 */
infix fun Float.format(precision: Int) = String.format("%.${precision}f", this)

operator fun PointF.plus(pointF: PointF) = PointF(x + pointF.x, y + pointF.y)

operator fun PointF.minus(pointF: PointF) = PointF(x - pointF.x, y - pointF.y)

operator fun PointF.times(pointF: PointF) = x * pointF.x + y * pointF.y

operator fun PointF.div(float: Float) = if (float != 0f) PointF(x / float, y / float) else throw IllegalArgumentException("divider is zero")

fun PointF.distance(pointF: PointF = PointF(0f, 0f)) = Math.sqrt((this - pointF).let { it * it }.toDouble())

class MeasureModeGestureListener(
        private val measure: ImageFrameStore.Measure,
        dispatch: (Any) -> Unit
) : ScaleCompositeGestureListener(dispatch) {
    private val points = Stack<PointF>()
    private val lengthThreshold = 5f
    private val precision = 2

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                points.push(getPoint(motionEvent))
                dispatch(ImageActions.drawLines(points = *points.toTypedArray(), showMagnify = true))
            }
            MotionEvent.ACTION_MOVE -> {
                points.pop()
                points.push(getPoint(motionEvent))
                dispatch(ImageActions.drawLines(points = *points.toTypedArray(), showMagnify = true))
            }
            MotionEvent.ACTION_UP -> {
                points.pop()
                points.push(getPoint(motionEvent))
                if (points.size > 1) {
                    when (measure) {
                        ImageFrameStore.Measure.NONE -> throw IllegalStateException("Unexpected measure mode")
                        ImageFrameStore.Measure.LINE -> {
                            length(points.first(), points.last()).takeIf { it > lengthThreshold }?.let {
                                dispatch(ImageActions.addPath(points.toList(), textLocation(points.first(), points.last()) to (it.toFloat() format precision)))
                            }
                            points.clear()
                        }
                        ImageFrameStore.Measure.ANGEL -> {
                            if (points.size == 3) {
                                val length = length(points[1], points[2])
                                if (length >= lengthThreshold) {
                                    dispatch(ImageActions.addPath(
                                            points.toList(),
                                            points[1] to calculateAngle(points[0], points[1], points[2]).run {
                                                buildString {
                                                    append("∠")
                                                    append(this@run format precision)
                                                    append("°")
                                                }
                                            }
                                    ))
                                    points.clear()
                                } else {
                                    points.pop()
                                    dispatch(ImageActions.drawLines(*points.toTypedArray()))
                                }
                            } else {
                                val length = length(points.first(), points.last())
                                if (length < lengthThreshold) {
                                    points.pop()
                                }
                                dispatch(ImageActions.drawLines(*points.toTypedArray()))
                            }
                        }
                    }
                } else {
                    dispatch(ImageActions.drawLines(*points.toTypedArray()))
                }
            }
        }
        return true
    }

    private fun getPoint(motionEvent: MotionEvent) = PointF(motionEvent.x, motionEvent.y)

    private fun length(point1: PointF, point2: PointF) = point1.distance(point2)

    private fun textLocation(startPoint: PointF, endPoint: PointF) = (startPoint + endPoint) / 2f

    private fun calculateAngle(startPoint: PointF, anglePoint: PointF, endPoint: PointF) = ((startPoint - anglePoint) to (endPoint - anglePoint)).let {
        ((it.first * it.second) / (it.first.distance() * it.second.distance())).let {
            (Math.acos(it) * 180 / Math.PI).toFloat()
        }
    }
}