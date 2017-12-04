package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.Path
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.event.RequireRedrawCanvas
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState

/**
 * Created by charleslzq on 17-11-30.
 */
class MeasureModeGestureListener(
        state: ImageFramesViewState
) : ScaleCompositeGestureListener(state) {
    lateinit var startPoint: PointF
    lateinit var secondPoint: PointF

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                val currentPoint = getPoint(motionEvent)
                if (framesViewState.firstPath) {
                    startPoint = currentPoint
                } else {
                    framesViewState.currentPath.reset()
                    framesViewState.currentPath.moveTo(startPoint.x, startPoint.y)
                    framesViewState.currentPath.lineTo(secondPoint.x, secondPoint.y)
                    framesViewState.currentPath.lineTo(currentPoint.x, currentPoint.y)
                }
                EventBus.send(RequireRedrawCanvas())
            }
            MotionEvent.ACTION_MOVE -> {
                val currentPoint = getPoint(motionEvent)
                framesViewState.currentPath.reset()
                framesViewState.currentPath.moveTo(startPoint.x, startPoint.y)
                if (!framesViewState.firstPath) {
                    framesViewState.currentPath.lineTo(secondPoint.x, secondPoint.y)
                }
                framesViewState.currentPath.lineTo(currentPoint.x, currentPoint.y)
                EventBus.send(RequireRedrawCanvas())
            }
            MotionEvent.ACTION_UP -> {
                val currentPoint = getPoint(motionEvent)
                val length = if (framesViewState.firstPath) length(startPoint, currentPoint) else length(secondPoint, currentPoint)
                if (length > 5.0) {
                    if (framesViewState.measure == ImageFramesViewState.Measure.LINE) {
                        val text = length.toString()
                        framesViewState.currentPath.reset()
                        framesViewState.currentPath.moveTo(startPoint.x, startPoint.y)
                        framesViewState.currentPath.lineTo(currentPoint.x, currentPoint.y)

                        framesViewState.textList.add(currentPoint to text)
                        framesViewState.pathList.add(framesViewState.currentPath)
                        framesViewState.currentPath = Path()
                    } else if (framesViewState.measure == ImageFramesViewState.Measure.ANGEL) {
                        if (framesViewState.firstPath) {
                            framesViewState.currentPath.reset()
                            framesViewState.currentPath.moveTo(startPoint.x, startPoint.y)
                            framesViewState.currentPath.lineTo(currentPoint.x, currentPoint.y)
                            secondPoint = currentPoint
                            framesViewState.firstPath = false
                        } else {
                            framesViewState.currentPath.reset()
                            framesViewState.currentPath.moveTo(startPoint.x, startPoint.y)
                            framesViewState.currentPath.lineTo(secondPoint.x, secondPoint.y)
                            framesViewState.currentPath.lineTo(currentPoint.x, currentPoint.y)

                            val text = calculateAngle(secondPoint, currentPoint)
                            framesViewState.pathList.add(framesViewState.currentPath)
                            framesViewState.textList.add(secondPoint to text.toString())

                            framesViewState.currentPath = Path()
                            framesViewState.firstPath = true
                        }
                    }
                }
                EventBus.send(RequireRedrawCanvas())
            }
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        framesViewState.pathList.clear()
        framesViewState.textList.clear()
        EventBus.send(RequireRedrawCanvas())
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