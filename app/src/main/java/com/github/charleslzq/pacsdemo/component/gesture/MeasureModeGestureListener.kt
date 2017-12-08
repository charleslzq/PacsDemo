package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.Path
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.event.RequireRedrawCanvas
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-30.
 */
class MeasureModeGestureListener(
        val framesStore: ImageFramesStore,
        layoutPosition: Int
) : ScaleCompositeGestureListener(layoutPosition) {
    lateinit var startPoint: PointF
    lateinit var secondPoint: PointF

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                val currentPoint = getPoint(motionEvent)
                if (framesStore.firstPath) {
                    startPoint = currentPoint
                } else {
                    framesStore.currentPath.reset()
                    framesStore.currentPath.moveTo(startPoint.x, startPoint.y)
                    framesStore.currentPath.lineTo(secondPoint.x, secondPoint.y)
                    framesStore.currentPath.lineTo(currentPoint.x, currentPoint.y)
                }
                EventBus.post(RequireRedrawCanvas())
            }
            MotionEvent.ACTION_MOVE -> {
                val currentPoint = getPoint(motionEvent)
                framesStore.currentPath.reset()
                framesStore.currentPath.moveTo(startPoint.x, startPoint.y)
                if (!framesStore.firstPath) {
                    framesStore.currentPath.lineTo(secondPoint.x, secondPoint.y)
                }
                framesStore.currentPath.lineTo(currentPoint.x, currentPoint.y)
                EventBus.post(RequireRedrawCanvas())
            }
            MotionEvent.ACTION_UP -> {
                val currentPoint = getPoint(motionEvent)
                val length = if (framesStore.firstPath) length(startPoint, currentPoint) else length(secondPoint, currentPoint)
                if (length > 5.0) {
                    if (framesStore.measure == ImageFramesStore.Measure.LINE) {
                        val text = length.toString()
                        framesStore.currentPath.reset()
                        framesStore.currentPath.moveTo(startPoint.x, startPoint.y)
                        framesStore.currentPath.lineTo(currentPoint.x, currentPoint.y)

                        framesStore.textList.add(currentPoint to text)
                        framesStore.pathList.add(framesStore.currentPath)
                        framesStore.currentPath = Path()
                    } else if (framesStore.measure == ImageFramesStore.Measure.ANGEL) {
                        if (framesStore.firstPath) {
                            framesStore.currentPath.reset()
                            framesStore.currentPath.moveTo(startPoint.x, startPoint.y)
                            framesStore.currentPath.lineTo(currentPoint.x, currentPoint.y)
                            secondPoint = currentPoint
                            framesStore.firstPath = false
                        } else {
                            framesStore.currentPath.reset()
                            framesStore.currentPath.moveTo(startPoint.x, startPoint.y)
                            framesStore.currentPath.lineTo(secondPoint.x, secondPoint.y)
                            framesStore.currentPath.lineTo(currentPoint.x, currentPoint.y)

                            val text = calculateAngle(secondPoint, currentPoint)
                            framesStore.pathList.add(framesStore.currentPath)
                            framesStore.textList.add(secondPoint to text.toString())

                            framesStore.currentPath = Path()
                            framesStore.firstPath = true
                        }
                    }
                }
                EventBus.post(RequireRedrawCanvas())
            }
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        framesStore.measure = ImageFramesStore.Measure.NONE
        return true
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