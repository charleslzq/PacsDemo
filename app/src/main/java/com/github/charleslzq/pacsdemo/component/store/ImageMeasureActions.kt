package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import com.github.charleslzq.kotlin.react.DispatchAction
import com.github.charleslzq.kotlin.react.castOrNull
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.*
import com.github.charleslzq.pacsdemo.support.UndoSupport
import com.github.charleslzq.pacsdemo.support.applyCanvas
import com.github.charleslzq.pacsdemo.support.runOnCompute
import java.util.*

/**
 * Created by charleslzq on 17-12-28.
 */
infix fun Float.format(precision: Int) = String.format("%.${precision}f", this)

infix fun Double.format(precision: Int) = String.format("%.${precision}f", this)

operator fun PointF.plus(pointF: PointF) = PointF(x + pointF.x, y + pointF.y)

operator fun PointF.minus(pointF: PointF) = PointF(x - pointF.x, y - pointF.y)

operator fun PointF.times(pointF: PointF) = x * pointF.x + y * pointF.y

operator fun PointF.div(float: Float) = if (float != 0f) PointF(
    x / float,
    y / float
) else throw IllegalArgumentException("divider is zero")

/**
 * 计算两点之间的距离
 * @param pointF 另一个点, 默认值为原点
 */
fun PointF.distance(pointF: PointF = PointF(0f, 0f)) =
    Math.sqrt((this - pointF).let { it * it }.toDouble())

object ImageMeasureActions {
    private const val PRECISION = 2

    /**
     * @param point 选取的点
     * @param replaceLast 是否替换上一个选取的点
     * @param showMagnify 是否显示放大镜
     * @return 选择一个点的操作
     */
    fun selectPoint(point: PointF, replaceLast: Boolean, showMagnify: Boolean) = buildAction {
        runOnCompute {
            if (replaceLast && points.isNotEmpty()) {
                points.pop()
            }
            points.push(mapPoint(store, point))
            val leftTopPoint = mapPoint(store, PointF(0f, 0f))
            val rightBottomPoint = getCurrentImage(store)!!.let {
                mapPoint(
                    store,
                    PointF(store.viewWidth, store.viewHeight)
                )
            }
            val width = (rightBottomPoint.x - leftTopPoint.x).toInt()
            val height = (rightBottomPoint.y - leftTopPoint.y).toInt()
            val drawingText = when {
                !showMagnify && points.size == 2 && store.measure == Measure.LINE -> calculateLineText(
                    points,
                    store,
                    width,
                    height
                )
                !showMagnify && points.size == 3 && store.measure == Measure.ANGEL -> calculateAngleText(
                    points,
                    store,
                    width,
                    height
                )
                else -> null
            }
            if (drawingText != null) {
                dispatch(
                    ImageCanvasModel(
                        drawNewMeasureResult(store, undoSupport, points, drawingText),
                        null,
                        undoSupport.canUndo(),
                        undoSupport.canRedo()
                    )
                )
                points.clear()
            } else {
                drawTmpMeasure(store, dispatch, points, undoSupport, showMagnify, width, height)
            }
        }
    }

    /**
     * @return 撤销上一次绘制的操作(包括未完成的绘制)
     */
    fun undoDrawing() = buildAction {
        if (points.isNotEmpty()) {
            runOnCompute {
                val width = getCurrentImage(store)!!.width
                val height = getCurrentImage(store)!!.height
                points.pop()
                drawTmpMeasure(store, dispatch, points, undoSupport, false, width, height)
            }
        } else if (undoSupport.canUndo()) {
            runOnCompute {
                dispatch(
                    ImageCanvasModel(
                        undoSupport.undo(),
                        null,
                        undoSupport.canUndo(),
                        undoSupport.canRedo()
                    )
                )
            }
        }
    }

    /**
     * @return 重做上一次撤销的操作(不包括撤销的未完成操作)
     */
    fun redoDrawing() = buildAction {
        if (undoSupport.canRedo()) {
            runOnCompute {
                dispatch(
                    ImageCanvasModel(
                        undoSupport.redo(),
                        null,
                        undoSupport.canUndo(),
                        undoSupport.canRedo()
                    )
                )
                points.clear()
            }
        }
    }

    /**
     * @return 清除绘制的操作
     */
    fun clearDrawing() = buildAction {
        runOnCompute {
            undoSupport.reset()
            points.clear()
            dispatch(ClearMeasure())
        }
    }

    /**
     * @return 移动撤销/重做栈的操作
     */
    fun moveStackFrom(imageFrameStore: ImageFrameStore) = buildAction {
        undoSupport.reset()
        points.clear()

        imageFrameStore.dispatch(moveStackTo(undoSupport, points))
    }

    private fun moveStackTo(targetUndoSupport: UndoSupport<Bitmap>, targetPoints: Stack<PointF>) =
        buildAction {
            targetUndoSupport.copyFrom(undoSupport)
            points.forEach { targetPoints.push(it) }

            undoSupport.reset()
            points.clear()
        }

    /**
     * 创建与当前图像相同大小的bitmap用于绘制
     */
    private fun createDrawingBase(store: ImageFrameStore) =
        getCurrentImage(store)?.let {
            Bitmap.createBitmap(it.width, it.height, it.config)
        }

    /**
     * 将触摸选取的点坐标转换成原始图像上的点坐标
     */
    private fun mapPoint(store: ImageFrameStore, point: PointF) =
        FloatArray(2).apply {
            this[0] = point.x
            this[1] = point.y
            store.getInvertMatrix().mapPoints(this)
        }.let { PointF(it[0], it[1]) }

    private fun getCurrentImage(store: ImageFrameStore) =
        if (store.displayModel.images.isNotEmpty()) {
            store.displayModel.images[0]
        } else {
            null
        }

    /**
     * 将点列表转换成坐标列表,供canvas绘制用
     */
    private fun toLines(vararg points: PointF) = when (points.size) {
        0 -> FloatArray(0)
        1 -> FloatArray(2).apply {
            val point = points.first()
            this[0] = point.x
            this[1] = point.y
        }
        else -> FloatArray((points.size - 1) * 4).apply {
            repeat(points.size - 1) {
                val start = it * 4
                this[start] = points[it].x
                this[start + 1] = points[it].y
                this[start + 2] = points[it + 1].x
                this[start + 3] = points[it + 1].y
            }
        }
    }

    /**
     * 计算三个点的夹角
     */
    private fun calculateAngle(startPoint: PointF, anglePoint: PointF, endPoint: PointF) =
        ((startPoint - anglePoint) to (endPoint - anglePoint)).let {
            ((it.first * it.second) / (it.first.distance() * it.second.distance())).let {
                (Math.acos(it) * 180 / Math.PI).toFloat()
            }
        }

    private fun calculateLineText(
        points: Stack<PointF>,
        store: ImageFrameStore,
        width: Int,
        height: Int
    ): Pair<PointF, String> {
        val text = (points.first().distance(points.last()) / store.scale) format PRECISION
        val rawLocation = (points.first() + points.last()) / 2f
        return Rect().let {
            store.stringPaint.getTextBounds(text, 0, text.length, it)
            if ((points.first().x - points.last().x) * (points.first().y - points.last().y) >= 0) {
                if (rawLocation.x + it.width() > width || rawLocation.y - it.height() < 0) {
                    PointF(rawLocation.x - it.width(), rawLocation.y + it.height())
                } else {
                    PointF(rawLocation.x, rawLocation.y - it.height())
                }
            } else {
                if (rawLocation.x + it.width() > width || rawLocation.y + it.height() > height) {
                    PointF(rawLocation.x - it.width(), rawLocation.y - it.height())
                } else {
                    PointF(rawLocation.x, rawLocation.y + it.height())
                }
            }
        } to text
    }

    private fun calculateAngleText(
        points: Stack<PointF>,
        store: ImageFrameStore,
        width: Int,
        height: Int
    ): Pair<PointF, String> {
        val text = buildString {
            append("∠")
            append(calculateAngle(points[0], points[1], points[2]) format PRECISION)
            append("°")
        }
        val rawLocation = points[1]
        return Rect().let {
            store.stringPaint.getTextBounds(text, 0, text.length, it)
            val startPoint = arrayOf(
                PointF(rawLocation.x, rawLocation.y + it.height()),
                PointF(rawLocation.x - it.width(), rawLocation.y + it.height()),
                PointF(rawLocation.x - it.width(), rawLocation.y),
                PointF(rawLocation.x, rawLocation.y)
            )
            val outOfRange = arrayOf(
                rawLocation.x + it.width() > width || rawLocation.y + it.height() > height,
                rawLocation.x - it.width() < 0 || rawLocation.y + it.height() > height,
                rawLocation.x - it.width() < 0 || rawLocation.y - it.height() < 0,
                rawLocation.x + it.width() > width || rawLocation.y - it.height() < 0
            )
            val lineOccupy = arrayOf(false, false, false, false)
            arrayOf(points[0], points[2]).forEach {
                val offset = it - points[1]
                when {
                    offset.x > 0 && offset.y > 0 -> lineOccupy[0] = true
                    offset.x < 0 && offset.y > 0 -> lineOccupy[1] = true
                    offset.x < 0 && offset.y < 0 -> lineOccupy[2] = true
                    offset.x > 0 && offset.y < 0 -> lineOccupy[3] = true
                }
            }
            outOfRange.indices.filter { !outOfRange[it] }.run {
                startPoint[find { !lineOccupy[it] } ?: first()]
            }
        } to text
    }

    private fun drawNewMeasureResult(
        store: ImageFrameStore,
        undoSupport: UndoSupport<Bitmap>,
        points: Stack<PointF>,
        drawingText: Pair<PointF, String>
    ) = undoSupport.generate({ createDrawingBase(store)!! }) {
        Bitmap.createBitmap(it.width, it.height, it.config).applyCanvas {
            drawBitmap(it, 0f, 0f, store.linePaint)
            drawPath(Path().apply {
                moveTo(points[0].x, points[0].y)
                repeat(points.size - 1) {
                    lineTo(points[it + 1].x, points[it + 1].y)
                }
            }, store.linePaint)
            drawText(
                drawingText.second,
                drawingText.first.x,
                drawingText.first.y,
                store.stringPaint
            )
        }
    }

    private fun drawTmpMeasure(
        store: ImageFrameStore, dispatch: (Any) -> Unit, points: Stack<PointF>,
        undoSupport: UndoSupport<Bitmap>, showMagnify: Boolean, width: Int, height: Int
    ) {
        if (points.isEmpty()) {
            dispatch(DrawLines(null, undoSupport.canUndo()))
        } else {
            val coordinates = toLines(*points.toTypedArray())
            if (store.hasImage) {
                createDrawingBase(store)?.let {
                    dispatch(DrawLines(it.applyCanvas {
                        if (coordinates.size > 3) {
                            drawLines(coordinates, store.linePaint)
                        }
                        val lastX = points.last().x.toInt()
                        val lastY = points.last().y.toInt()
                        if (showMagnify) {
                            getCurrentImage(store)?.run {
                                val range = arrayOf(
                                    (store.range / store.scale).toInt(),
                                    width - lastX,
                                    lastX,
                                    height - lastY,
                                    lastY
                                ).min()!!
                                val magnifyRange =
                                    arrayOf(width - lastX, lastX, height - lastY, lastY).max()!!
                                val magnify = Math.min(
                                    if (range > 20) 3 else (60f / range).toInt(),
                                    (magnifyRange.toFloat() / (2 * range)).toInt()
                                )
                                if (magnify > 1) {
                                    val srcRect = Rect(
                                        lastX - range,
                                        lastY - range,
                                        lastX + range,
                                        lastY + range
                                    )
                                    val startX = if (lastX > 2 * magnify * range) {
                                        lastX - 2 * magnify * range
                                    } else {
                                        lastX
                                    }
                                    val startY = if (lastY > 2 * magnify * range) {
                                        lastY - 2 * magnify * range
                                    } else {
                                        lastY
                                    }
                                    val dstRect = Rect(
                                        startX,
                                        startY,
                                        startX + 2 * magnify * range,
                                        startY + 2 * magnify * range
                                    )
                                    val magnifyDistance = magnify.toFloat() * range
                                    val halfLineLength =
                                        Math.min(magnifyDistance, Math.max(10f, .5f * range))
                                    drawBitmap(this, srcRect, dstRect, store.linePaint)
                                    drawLine(
                                        startX + magnifyDistance,
                                        startY + magnifyDistance - halfLineLength,
                                        startX + magnifyDistance,
                                        startY + magnifyDistance + halfLineLength,
                                        store.linePaint
                                    )
                                    drawLine(
                                        startX + magnifyDistance - halfLineLength,
                                        startY + magnifyDistance,
                                        startX + magnifyDistance + halfLineLength,
                                        startY + magnifyDistance, store.linePaint
                                    )
                                }
                            }
                        } else {
                            drawCircle(
                                lastX.toFloat(),
                                lastY.toFloat(),
                                5f / store.scale,
                                store.pointPaint
                            )
                        }
                    }, true))
                }
            }
        }
    }

    private fun buildAction(handler: Context.() -> Unit): DispatchAction<ImageFrameStore> =
        { store, dispatch, args ->
            if (args.size != 2) {
                throw IllegalArgumentException("Wrong number of args! ${args.size}")
            } else {
                val undoSupport = castOrNull<UndoSupport<Bitmap>>(args[0])
                val points = castOrNull<Stack<PointF>>(args[1])
                if (undoSupport != null && points != null) {
                    handler(Context(store, dispatch, undoSupport, points))
                } else {
                    throw IllegalArgumentException("args must not be null!")
                }
            }
        }

    class Context(
        val store: ImageFrameStore,
        val dispatch: (Any) -> Unit,
        val undoSupport: UndoSupport<Bitmap>,
        val points: Stack<PointF>
    )
}