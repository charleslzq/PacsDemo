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
infix fun Float.format(precision: Int): String {
    return String.format("%.${precision}f", this)
}

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
                                dispatch(ImageActions.addPath(points.toList(), textLocation(points.first(), points.last()) to (it format precision)))
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

    private fun getPoint(motionEvent: MotionEvent): PointF {
        return PointF(motionEvent.x, motionEvent.y)
    }

    private fun length(point1: PointF, point2: PointF): Float {
        return Math.sqrt(((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y)).toDouble()).toFloat()
    }

    private fun textLocation(startPoint: PointF, endPoint: PointF): PointF {
        return PointF((startPoint.x + endPoint.x) / 2, (startPoint.y + endPoint.y) / 2)
    }

    private fun calculateAngle(startPoint: PointF, anglePoint: PointF, endPoint: PointF): Float {
        val offsetStart = PointF(startPoint.x - anglePoint.x, startPoint.y - anglePoint.y)
        val offsetEnd = PointF(endPoint.x - anglePoint.x, endPoint.y - anglePoint.y)
        val distanceStart = Math.sqrt((offsetStart.x * offsetStart.x + offsetStart.y * offsetStart.y).toDouble())
        val distanceEnd = Math.sqrt((offsetEnd.x * offsetEnd.x + offsetEnd.y * offsetEnd.y).toDouble())
        val cos = (offsetStart.x * offsetEnd.x + offsetEnd.y * offsetStart.y) / (distanceStart * distanceEnd)
        return (Math.acos(cos).toFloat() * 180 / Math.PI).toFloat()
    }
}