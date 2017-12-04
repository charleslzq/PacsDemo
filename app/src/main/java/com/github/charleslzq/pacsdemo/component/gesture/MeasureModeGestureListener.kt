package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState

/**
 * Created by charleslzq on 17-11-30.
 */
class MeasureModeGestureListener(
        val imageView: ImageView,
        framesViewState: ImageFramesViewState
) : ScaleCompositeGestureListener(framesViewState) {
    private lateinit var canvas: Canvas
    private lateinit var startPoint: PointF
    private lateinit var secondPoint: PointF
    private var path = Path()
    private val pathList = mutableListOf<Path>()
    private val textList = mutableListOf<Pair<PointF, String>>()
    private var firstPath = true

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                val currentPoint = getPoint(motionEvent)
                if (firstPath) {
                    startPoint = currentPoint
                } else {
                    path.reset()
                    path.moveTo(startPoint.x, startPoint.y)
                    path.lineTo(secondPoint.x, secondPoint.y)
                    path.lineTo(currentPoint.x, currentPoint.y)
                }
                resetCanvas()
            }
            MotionEvent.ACTION_MOVE -> {
                val currentPoint = getPoint(motionEvent)
                path.reset()
                path.moveTo(startPoint.x, startPoint.y)
                if (!firstPath) {
                    path.lineTo(secondPoint.x, secondPoint.y)
                }
                path.lineTo(currentPoint.x, currentPoint.y)
                resetCanvas()
            }
            MotionEvent.ACTION_UP -> {
                val currentPoint = getPoint(motionEvent)
                val length = if (firstPath) length(startPoint, currentPoint) else length(secondPoint, currentPoint)
                if (length > 5.0) {
                    if (framesViewState.measure == ImageFramesViewState.Measure.LINE) {
                        val text = length.toString()
                        path.reset()
                        path.moveTo(startPoint.x, startPoint.y)
                        path.lineTo(currentPoint.x, currentPoint.y)

                        textList.add(currentPoint to text)
                        pathList.add(path)
                        path = Path()
                    } else if (framesViewState.measure == ImageFramesViewState.Measure.ANGEL) {
                        if (firstPath) {
                            path.reset()
                            path.moveTo(startPoint.x, startPoint.y)
                            path.lineTo(currentPoint.x, currentPoint.y)
                            secondPoint = currentPoint
                            firstPath = false
                        } else {
                            path.reset()
                            path.moveTo(startPoint.x, startPoint.y)
                            path.lineTo(secondPoint.x, secondPoint.y)
                            path.lineTo(currentPoint.x, currentPoint.y)

                            val text = calculateAngle(secondPoint, currentPoint)
                            pathList.add(path)
                            textList.add(secondPoint to text.toString())

                            path = Path()
                            firstPath = true
                        }
                    }
                }
                resetCanvas()
            }
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        pathList.clear()
        textList.clear()
        resetCanvas()
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

    private fun resetCanvas() {
        val bitmap = framesViewState.getScaledFrame(framesViewState.currentIndex)
        canvas = Canvas(bitmap)
        imageView.setImageBitmap(bitmap)

        pathList.forEach {
            canvas.drawPath(it, framesViewState.linePaint)
        }

        canvas.drawPath(path, framesViewState.linePaint)

        textList.forEach {
            canvas.drawText(it.second, it.first.x, it.first.y, framesViewState.stringPaint)
        }

        imageView.invalidate()
    }
}