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
class MeasureModeGestureListener(
        private val measure: ImageFrameStore.Measure,
        dispatch: (Any) -> Unit
) : ScaleCompositeGestureListener(dispatch) {
    private val points = Stack<PointF>()
    private val lengthThreshold = 5f

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                points.push(getPoint(motionEvent))
                dispatch(ImageActions.drawLines(*points.toTypedArray()))
            }
            MotionEvent.ACTION_MOVE -> {
                points.pop()
                points.push(getPoint(motionEvent))
                dispatch(ImageActions.drawLines(*points.toTypedArray()))
            }
            MotionEvent.ACTION_UP -> {
                points.pop()
                points.push(getPoint(motionEvent))
                if (points.size > 1) {
                    when (measure) {
                        ImageFrameStore.Measure.NONE -> throw IllegalStateException("Unexpected measure mode")
                        ImageFrameStore.Measure.LINE -> {
                            length(points.first(), points.last()).takeIf { it > lengthThreshold }?.let {
                                dispatch(ImageActions.addPath(points.toList(), points.last() to it.toString()))
                            }
                            points.clear()
                        }
                        ImageFrameStore.Measure.ANGEL -> {
                            if (points.size == 3) {
                                val length = length(points[1], points[2])
                                if (length >= lengthThreshold) {
                                    dispatch(ImageActions.addPath(points.toList(), points[1] to calculateAngle(points[0], points[1], points[2]).toString()))
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

    private fun calculateAngle(startPoint: PointF, anglePoint: PointF, endPoint: PointF): Float {
        val offsetStart = PointF(startPoint.x - anglePoint.x, startPoint.y - endPoint.y)
        val offsetEnd = PointF(endPoint.x - anglePoint.x, endPoint.y - anglePoint.y)
        val distanceStart = Math.sqrt((offsetStart.x * offsetStart.x + offsetStart.y * offsetStart.y).toDouble())
        val distanceEnd = Math.sqrt((offsetEnd.x * offsetEnd.x + offsetEnd.y * offsetEnd.y).toDouble())
        val cos = (offsetStart.x * offsetEnd.x + offsetEnd.y * offsetStart.y) / (distanceStart * distanceEnd)
        return (Math.acos(cos).toFloat() * 180 / Math.PI).toFloat()
    }
}