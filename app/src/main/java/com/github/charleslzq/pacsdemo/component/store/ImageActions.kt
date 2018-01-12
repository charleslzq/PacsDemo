package com.github.charleslzq.pacsdemo.component.store

import android.graphics.*
import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import com.github.charleslzq.kotlin.react.DispatchAction
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.*
import com.github.charleslzq.pacsdemo.support.BitmapCache
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import com.github.charleslzq.pacsdemo.support.UndoSupport
import java.net.URI
import java.util.*

/**
 * Created by charleslzq on 17-12-28.
 */
infix fun Float.format(precision: Int) = String.format("%.${precision}f", this)

infix fun Double.format(precision: Int) = String.format("%.${precision}f", this)

operator fun PointF.plus(pointF: PointF) = PointF(x + pointF.x, y + pointF.y)

operator fun PointF.minus(pointF: PointF) = PointF(x - pointF.x, y - pointF.y)

operator fun PointF.times(pointF: PointF) = x * pointF.x + y * pointF.y

operator fun PointF.div(float: Float) = if (float != 0f) PointF(x / float, y / float) else throw IllegalArgumentException("divider is zero")

fun PointF.distance(pointF: PointF = PointF(0f, 0f)) = Math.sqrt((this - pointF).let { it * it }.toDouble())

data class PatientSeriesModel(
        val modId: String = "",
        val patientMetaInfo: DicomPatientMetaInfo = DicomPatientMetaInfo(),
        val studyMetaInfo: DicomStudyMetaInfo = DicomStudyMetaInfo(),
        val seriesMetaInfo: DicomSeriesMetaInfo = DicomSeriesMetaInfo(),
        val frames: List<ImageFrameModel> = emptyList(),
        val thumb: URI? = null
)

object ImageActions : RxScheduleSupport {
    private val seriesModels: MutableList<PatientSeriesModel> = mutableListOf()
    private val undoSupports = (0..8).map { UndoSupport<Bitmap>() }
    private val pointsStacks = (0..8).map { Stack<PointF>() }
    private var bitmapCache = BitmapCache()
    private val preloadRange = 5
    private val precision = 2

    fun reloadModels(patientSeriesModelList: List<PatientSeriesModel>): DispatchAction<PacsStore> {
        seriesModels.clear()
        seriesModels.addAll(patientSeriesModelList)
        val thumbList = callOnIo {
            seriesModels.filter { it.modId.isNotBlank() && it.thumb != null }
                    .mapNotNull { BitmapCache.decode(it.thumb!!)?.let { thumb -> ImageThumbModel(it.modId, thumb) } }
        }
        return { _, dispatch, _ ->
            dispatch(PacsStore.SeriesListUpdated(thumbList))
        }
    }

    fun changeLayout(layoutOrdinal: Int): DispatchAction<PacsStore> = { store, _, _ ->
        val ordinal = layoutOrdinal.rem(PacsStore.LayoutOption.values().size).let { if (it < 0) it + PacsStore.LayoutOption.values().size else it }
        store.dispatch(changeLayout(PacsStore.LayoutOption.values()[ordinal]))
    }

    fun changeLayout(layoutOption: PacsStore.LayoutOption): DispatchAction<PacsStore> = { store, dispatch, _ ->
        if (store.layoutOption != layoutOption) {
            dispatch(PacsStore.ChangeLayout(layoutOption.ordinal))
            store.imageCells.forEach {
                cleanMeasure(it, it.dispatch)
                it.dispatch(Reset())
                it.dispatch(
                        if (layoutOption == PacsStore.LayoutOption.ONE_ONE) {
                            AllowPlay()
                        } else {
                            ForbidPlay()
                        }
                )
            }
            bitmapCache = BitmapCache(100)
        }
    }

    fun bindModel(modId: String, index: Int = 0): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnIo {
            cleanMeasure(store, dispatch)
            seriesModels.find { it.modId == modId }?.let {
                dispatch(BindModel(modId, it.patientMetaInfo, it.studyMetaInfo, it.seriesMetaInfo, it.frames.size))
                findImage(it, index)?.run {
                    dispatch(ShowImage(this, index, it.frames[index].meta))
                }
                if (store.playable) {
                    bitmapCache = BitmapCache(Math.max(100, it.frames.size))
                    bitmapCache.preload(*it.frames.map { it.frame }.toTypedArray())
                } else {
                    bitmapCache.preload(*urisInRange(it, index - preloadRange, index + preloadRange).toTypedArray())
                }
            }
        }
    }

    fun moveFrame(imageFrameStore: ImageFrameStore): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        dispatch(Reset())
        store.dispatch(ImageActions.bindModel(imageFrameStore.bindModId, imageFrameStore.index))
        if (imageFrameStore.reverseColor) {
            dispatch(ReverseColor())
        }
        if (imageFrameStore.pseudoColor) {
            dispatch(PseudoColor())
        }
        undoSupports[store.layoutPosition].copyFrom(undoSupports[imageFrameStore.layoutPosition])
        undoSupports[imageFrameStore.layoutPosition].reset()
        dispatch(imageFrameStore.canvasModel)

        imageFrameStore.dispatch(ImageFrameStore.Reset())
    }

    fun playOrPause(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        if (store.playable) {
            runOnIo {
                if (store.displayModel.images.size > 1 && store.autoJumpIndex != 0) {
                    dispatchShowImage(store.bindModId, store.index, dispatch)
                } else {
                    seriesModels.find { it.modId == store.bindModId }?.let {
                        dispatch(PlayAnimation(findFrames(it, store.autoJumpIndex)))
                    }
                }
            }
        }
    }

    fun showImage(index: Int): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnIo {
            dispatchShowImage(store.bindModId, index, dispatch)
        }
    }

    fun playIndexChange(index: Int): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        seriesModels.find { it.modId == store.bindModId }?.let {
            if (index in 0 until it.frames.size) {
                dispatch(PlayIndexChange(index, it.frames[index].meta))
            }
        }
    }

    fun indexScroll(scrollDistance: Float): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnIo {
            if (store.size > 0) {
                val changeBase = Math.min(300f / store.size, 10f)
                val offset = (scrollDistance / changeBase).toInt()
                val newIndex = Math.min(Math.max(store.index - offset, 0), store.size - 1)
                dispatchShowImage(store.bindModId, newIndex, dispatch)
            }
        }
    }

    fun resetDisplay(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnIo {
            cleanMeasure(store, dispatch)
            dispatch(ResetDisplay())
            if (store.size > 1) {
                dispatchShowImage(store.bindModId, 0, dispatch)
            }
        }
    }

    fun selectPoint(point: PointF, replaceLast: Boolean, showMagnify: Boolean): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnCompute {
            val canvasBaseStack = undoSupports[store.layoutPosition]
            val points = pointsStacks[store.layoutPosition]
            if (replaceLast && points.isNotEmpty()) {
                points.pop()
            }
            points.push(point)
            val width = getCurrentImage(store)!!.width
            val height = getCurrentImage(store)!!.height
            val drawingText = when {
                !showMagnify && points.size == 2 && store.measure == Measure.LINE -> calculateLineText(points, store, width, height)
                !showMagnify && points.size == 3 && store.measure == Measure.ANGEL -> calculateAngleText(points, store, width, height)
                else -> null
            }
            if (drawingText != null) {
                dispatch(ImageCanvasModel(drawNewMeasureResult(store, points, drawingText),
                        null,
                        canvasBaseStack.canUndo(),
                        canvasBaseStack.canRedo()))
                points.clear()
            } else {
                drawTmpMeasure(points, store, dispatch, showMagnify, width, height)
            }
        }
    }

    fun undoDrawing(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        val stack = undoSupports[store.layoutPosition]
        val points = pointsStacks[store.layoutPosition]
        if (points.isNotEmpty()) {
            runOnCompute {
                val width = getCurrentImage(store)!!.width
                val height = getCurrentImage(store)!!.height
                points.pop()
                drawTmpMeasure(points, store, dispatch, false, width, height)
            }
        } else if (stack.canUndo()) {
            runOnCompute {
                dispatch(ImageCanvasModel(stack.undo(), null, stack.canUndo(), stack.canRedo()))
            }
        }
    }

    fun redoDrawing(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        val stack = undoSupports[store.layoutPosition]
        if (stack.canRedo()) {
            runOnCompute {
                dispatch(ImageCanvasModel(stack.redo(), null, stack.canUndo(), stack.canRedo()))
                pointsStacks[store.layoutPosition].clear()
            }
        }
    }

    fun clearDrawing(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnCompute {
            undoSupports[store.layoutPosition].reset()
            pointsStacks[store.layoutPosition].clear()
            dispatch(ClearMeasure())
        }
    }

    private fun urisInRange(patientSeriesModel: PatientSeriesModel, start: Int, end: Int)
            = patientSeriesModel.frames.subList(Math.max(0, start), Math.min(end + 1, patientSeriesModel.frames.size)).map { it.frame }

    private fun dispatchShowImage(modId: String, index: Int, dispatch: (Any) -> Unit) = seriesModels.find { it.modId == modId }?.let {
        findImage(it, index)?.run {
            dispatch(ShowImage(this, index, it.frames[index].meta))
            bitmapCache.preload(*urisInRange(it, index - preloadRange, index + preloadRange).toTypedArray())
        }
    }

    private fun findImage(model: PatientSeriesModel, index: Int = 0) = when {
        model.frames.isEmpty() || index !in (0 until model.frames.size) -> null
        else -> loadImage(model.frames[index].frame)
    }

    private fun findFrames(model: PatientSeriesModel, index: Int = 0) = when {
        model.frames.isEmpty() || index !in (0 until model.frames.size) -> emptyList()
        else -> model.frames.subList(index, model.frames.size).mapNotNull { loadImage(it.frame) }
    }

    private fun createDrawingBase(store: ImageFrameStore) = getCurrentImage(store)?.let { Bitmap.createBitmap(it.width, it.height, it.config) }

    private fun getCurrentImage(store: ImageFrameStore) = if (store.displayModel.images.isNotEmpty()) {
        val rawBitmap = store.displayModel.images[0]
        if (store.scale != 1.0f) {
            val newWidth = (rawBitmap.width * store.scale).toInt()
            val newHeight = (rawBitmap.height * store.scale).toInt()
            Bitmap.createScaledBitmap(rawBitmap, newWidth, newHeight, false)
        } else {
            rawBitmap
        }
    } else {
        null
    }

    private fun cleanMeasure(store: ImageFrameStore, dispatch: (Any) -> Unit) {
        if (store.measure != Measure.NONE) {
            dispatch(ResetMeasure())
            pointsStacks[store.layoutPosition].clear()
            undoSupports[store.layoutPosition].reset()
        }
    }

    private fun loadImage(uri: URI) = bitmapCache.load(uri)

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

    private fun calculateAngle(startPoint: PointF, anglePoint: PointF, endPoint: PointF) = ((startPoint - anglePoint) to (endPoint - anglePoint)).let {
        ((it.first * it.second) / (it.first.distance() * it.second.distance())).let {
            (Math.acos(it) * 180 / Math.PI).toFloat()
        }
    }

    private fun calculateLineText(points: Stack<PointF>, store: ImageFrameStore, width: Int, height: Int): Pair<PointF, String> {
        val text = (points.first().distance(points.last()) / store.scale) format precision
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

    private fun calculateAngleText(points: Stack<PointF>, store: ImageFrameStore, width: Int, height: Int): Pair<PointF, String> {
        val text = calculateAngle(points[0], points[1], points[2]) format precision
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

    private fun drawNewMeasureResult(store: ImageFrameStore, points: Stack<PointF>, drawingText: Pair<PointF, String>): Bitmap {
        return undoSupports[store.layoutPosition].generate({ createDrawingBase(store)!! }) {
            Bitmap.createBitmap(it.width, it.height, it.config).apply {
                Canvas(this).apply {
                    drawBitmap(it, 0f, 0f, store.linePaint)
                    drawPath(Path().apply {
                        moveTo(points[0].x, points[0].y)
                        repeat(points.size - 1) {
                            lineTo(points[it + 1].x, points[it + 1].y)
                        }
                    }, store.linePaint)
                    drawText(drawingText.second, drawingText.first.x, drawingText.first.y, store.stringPaint)
                }
            }
        }
    }

    private fun drawTmpMeasure(points: Stack<PointF>, store: ImageFrameStore, dispatch: (Any) -> Unit, showMagnify: Boolean, width: Int, height: Int) {
        if (points.isEmpty()) {
            dispatch(DrawLines(null, undoSupports[store.layoutPosition].canUndo()))
        } else {
            val coordinates = toLines(*points.toTypedArray())
            if (store.hasImage) {
                createDrawingBase(store)?.let {
                    dispatch(DrawLines(it.apply {
                        Canvas(this).apply {
                            if (coordinates.size > 3) {
                                drawLines(coordinates, store.linePaint)
                            }
                            val lastX = points.last().x.toInt()
                            val lastY = points.last().y.toInt()
                            if (showMagnify) {
                                getCurrentImage(store)?.run {
                                    val range = arrayOf(store.range, width - lastX, lastX, height - lastY, lastY).min()!!
                                    val magnifyRange = arrayOf(width - lastX, lastX, height - lastY, lastY).max()!!
                                    val magnify = Math.min(if (range > 20) 3 else (60f / range).toInt(), (magnifyRange.toFloat() / (2 * range)).toInt())
                                    if (magnify > 1) {
                                        val srcRect = Rect(lastX - range, lastY - range, lastX + range, lastY + range)
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
                                        val dstRect = Rect(startX, startY, startX + 2 * magnify * range, startY + 2 * magnify * range)
                                        val magnifyDistance = magnify.toFloat() * range
                                        val halfLineLength = Math.min(magnifyDistance, Math.max(10f, .5f * range))
                                        drawBitmap(this, srcRect, dstRect, store.linePaint)
                                        drawLine(startX + magnifyDistance,
                                                startY + magnifyDistance - halfLineLength,
                                                startX + magnifyDistance,
                                                startY + magnifyDistance + halfLineLength, store.linePaint)
                                        drawLine(startX + magnifyDistance - halfLineLength,
                                                startY + magnifyDistance,
                                                startX + magnifyDistance + halfLineLength,
                                                startY + magnifyDistance, store.linePaint)
                                    }
                                }
                            } else {
                                drawCircle(lastX.toFloat(), lastY.toFloat(), 5f, store.pointPaint)
                            }
                        }
                    }, true))
                }
            }
        }
    }
}