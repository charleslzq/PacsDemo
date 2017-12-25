package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-30.
 */
class MeasureModeGestureListener(
        val framesStore: ImageFramesStore
) : ScaleCompositeGestureListener(framesStore.layoutPosition) {
    lateinit var startPoint: PointF
    lateinit var secondPoint: PointF
    private var firstPath = true

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                val currentPoint = getPoint(motionEvent)
                if (firstPath) {
                    startPoint = currentPoint
                } else {
                    dispatch(ImageDisplayEvent.DrawLines(layoutPosition, toLines(startPoint, secondPoint, currentPoint)))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val currentPoint = getPoint(motionEvent)
                val pointList = mutableListOf<PointF>()
                pointList.add(startPoint)
                if (!firstPath) {
                    pointList.add(secondPoint)
                }
                pointList.add(currentPoint)
                dispatch(ImageDisplayEvent.DrawLines(layoutPosition, toLines(*pointList.toTypedArray())))
            }
            MotionEvent.ACTION_UP -> {
                val currentPoint = getPoint(motionEvent)
                val length = if (firstPath) length(startPoint, currentPoint) else length(secondPoint, currentPoint)
                if (length > 5.0) {
                    if (framesStore.measure == ImageFramesStore.Measure.LINE) {
                        val text = length.toString()
                        dispatch(ImageDisplayEvent.AddPath(layoutPosition, listOf(startPoint, currentPoint), currentPoint to text))
                    } else if (framesStore.measure == ImageFramesStore.Measure.ANGEL) {
                        if (firstPath) {
                            dispatch(ImageDisplayEvent.DrawLines(layoutPosition, toLines(startPoint, currentPoint)))
                            secondPoint = currentPoint
                            firstPath = false
                        } else {
                            val text = calculateAngle(secondPoint, currentPoint).toString()
                            dispatch(ImageDisplayEvent.AddPath(layoutPosition, listOf(startPoint, secondPoint, currentPoint), secondPoint to text))
                            firstPath = true
                        }
                    }
                }
            }
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        dispatch(ImageDisplayEvent.MeasureModeReset(layoutPosition))
        return true
    }

    private fun toLines(vararg points: PointF): FloatArray {
        return FloatArray((points.size - 1) * 4).apply {
            (0..(points.size - 2)).forEach {
                val start = it * 4
                this[start] = points[it].x
                this[start + 1] = points[it].y
                this[start + 2] = points[it + 1].x
                this[start + 3] = points[it + 1].y
            }
        }
    }

    private fun getPoint(motionEvent: MotionEvent): PointF {
        return PointF(motionEvent.x, motionEvent.y)
    }

    private fun length(point1: PointF, point2: PointF): Float {
        return Math.sqrt(((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y)).toDouble()).toFloat()
    }

    private fun calculateAngle(anglePoint: PointF, endPoint: PointF): Float {
        val offsetStart = PointF(startPoint.x - anglePoint.x, startPoint.y - endPoint.y)
        val offsetEnd = PointF(endPoint.x - anglePoint.x, endPoint.y - anglePoint.y)
        val distanceStart = Math.sqrt((offsetStart.x * offsetStart.x + offsetStart.y * offsetStart.y).toDouble())
        val distanceEnd = Math.sqrt((offsetEnd.x * offsetEnd.x + offsetEnd.y * offsetEnd.y).toDouble())
        val cos = (offsetStart.x * offsetEnd.x + offsetEnd.y * offsetStart.y) / (distanceStart * distanceEnd)
        return (Math.acos(cos).toFloat() * 180 / Math.PI).toFloat()
    }
}