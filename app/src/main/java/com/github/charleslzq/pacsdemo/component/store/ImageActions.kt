package com.github.charleslzq.pacsdemo.component.store

import android.graphics.*
import com.github.charleslzq.kotlin.react.DispatchAction
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.*
import com.github.charleslzq.pacsdemo.support.BitmapCache
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import com.github.charleslzq.pacsdemo.support.UndoSupport
import java.net.URI

/**
 * Created by charleslzq on 17-12-28.
 */
object ImageActions : RxScheduleSupport {
    private val seriesModels: MutableList<PatientSeriesModel> = mutableListOf()
    private val stacks = (0..8).map { UndoSupport<Bitmap>() }
    private var bitmapCache = BitmapCache()
    private val preloadRange = 5

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

    fun changeLayout(layoutOrdinal: Int): DispatchAction<PacsStore> {
        return { store, _, _ ->
            val ordinal = layoutOrdinal.rem(PacsStore.LayoutOption.values().size).let { if (it < 0) it + PacsStore.LayoutOption.values().size else it }
            store.dispatch(changeLayout(PacsStore.LayoutOption.values()[ordinal]))
        }
    }

    fun changeLayout(layoutOption: PacsStore.LayoutOption): DispatchAction<PacsStore> {
        return { store, dispatch, _ ->
            if (store.layoutOption != layoutOption) {
                dispatch(PacsStore.ChangeLayout(layoutOption.ordinal))
                store.imageCells.forEach {
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
    }

    fun bindModel(modId: String, index: Int = 0): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                seriesModels.find { it.modId == modId }?.let {
                    dispatch(BindModel(modId, it.patientMetaInfo, it.studyMetaInfo, it.seriesMetaInfo, it.frames.size))
                    findImage(it, index)?.run {
                        dispatch(ShowImage(this, index, it.frames[index].meta))
                    }
                    if (store.playable) {
                        bitmapCache = BitmapCache(Math.max(100, it.frames.size))
                        bitmapCache.preload(*it.frames.map { it.frame }.toTypedArray())
                    } else {
                        bitmapCache.preload(*urisInRange(it, 0, 2 * preloadRange).toTypedArray())
                    }
                }
            }
        }
    }

    fun copyStates(imageFrameStore: ImageFrameStore): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            dispatch(Reset())
            store.dispatch(ImageActions.bindModel(imageFrameStore.bindModId, imageFrameStore.index))
            if (imageFrameStore.reverseColor) {
                dispatch(ReverseColor())
            }
            if (imageFrameStore.pseudoColor) {
                dispatch(PseudoColor())
            }
            stacks[store.layoutPosition].copyFrom(stacks[imageFrameStore.layoutPosition])
            stacks[imageFrameStore.layoutPosition].reset()
            dispatch(imageFrameStore.canvasModel)

            imageFrameStore.dispatch(ImageFrameStore.Reset())
        }
    }

    fun playOrPause(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
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
    }

    fun showImage(index: Int): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                dispatchShowImage(store.bindModId, index, dispatch)
            }
        }
    }

    fun playIndexChange(index: Int): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            seriesModels.find { it.modId == store.bindModId }?.let {
                if (index in 0 until it.frames.size) {
                    dispatch(PlayIndexChange(index, it.frames[index].meta))
                }
            }
        }
    }

    fun indexScroll(scrollDistance: Float): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                if (store.size > 0) {
                    val changeBase = Math.min(100f / store.size, 10f)
                    val offset = (scrollDistance / changeBase).toInt()
                    val newIndex = Math.min(Math.max(store.index - offset, 0), store.size - 1)
                    dispatchShowImage(store.bindModId, newIndex, dispatch)
                }
            }
        }
    }

    fun resetDisplay(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                cleanMeasure(store, dispatch)
                dispatch(ResetDisplay())
                if (store.size > 1) {
                    dispatchShowImage(store.bindModId, 0, dispatch)
                }
            }
        }
    }

    fun drawLines(vararg points: PointF, showMagnify: Boolean = false): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                val coordinates = toLines(*points)
                if (coordinates.size > 1 && store.hasImage) {
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
                                        val srcRect = Rect(lastX - range, lastY - range, lastX + range, lastY + range)
                                        val startX = if (lastX > 4 * range) {
                                            lastX - 4 * range
                                        } else {
                                            lastX
                                        }
                                        val startY = if (lastY > 4 * range) {
                                            lastY - 4 * range
                                        } else {
                                            lastY
                                        }
                                        val dstRect = Rect(startX, startY, startX + 4 * range, startY + 4 * range)
                                        drawBitmap(this, srcRect, dstRect, store.linePaint)
                                        drawLine(startX + 2.0f * range,
                                                startY + 1.5f * range,
                                                startX + 2.0f * range,
                                                startY + 2.5f * range, store.linePaint)
                                        drawLine(startX + 1.5f * range,
                                                startY + 2.0f * range,
                                                startX + 2.5f * range,
                                                startY + 2.0f * range, store.linePaint)
                                    }
                                } else {
                                    drawCircle(lastX.toFloat(), lastY.toFloat(), 5f, store.pointPaint)
                                }
                            }
                        }))
                    }
                }
            }
        }
    }

    fun addPath(points: List<PointF>, text: Pair<PointF, String>): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                val stack = stacks[store.layoutPosition]
                if (points.size > 1 && store.displayModel.images.isNotEmpty()) {
                    dispatch(ImageCanvasModel(
                            stack.generate({ createDrawingBase(store)!! }) {
                                Bitmap.createBitmap(it.width, it.height, it.config).apply {
                                    Canvas(this).apply {
                                        drawBitmap(it, 0f, 0f, store.linePaint)
                                        drawPath(Path().apply {
                                            moveTo(points[0].x, points[0].y)
                                            repeat(points.size - 1) {
                                                lineTo(points[it + 1].x, points[it + 1].y)
                                            }
                                        }, store.linePaint)
                                        val textLocation = Rect().let {
                                            store.stringPaint.getTextBounds(text.second, 0, text.second.length, it)
                                            when (points.size) {
                                                2 -> {
                                                    if ((points.first().x - points.last().x) * (points.first().y - points.last().y) >= 0) {
                                                        if (text.first.x + it.width() > width || text.first.y - it.height() < 0) {
                                                            PointF(text.first.x - it.width(), text.first.y + it.height())
                                                        } else {
                                                            PointF(text.first.x, text.first.y - it.height())
                                                        }
                                                    } else {
                                                        if (text.first.x + it.width() > width || text.first.y + it.height() > height) {
                                                            PointF(text.first.x - it.width(), text.first.y - it.height())
                                                        } else {
                                                            PointF(text.first.x, text.first.y + it.height())
                                                        }
                                                    }
                                                }
                                                3 -> {
                                                    if (text.first.x + it.width() <= width && text.first.y + it.height() <= height) {
                                                        PointF(text.first.x, text.first.y + it.height())
                                                    } else if (text.first.x - it.width() >= 0 && text.first.y + it.height() <= height) {
                                                        PointF(text.first.x - it.width(), text.first.y + it.height())
                                                    } else if (text.first.x - it.width() >= 0 && text.first.y + it.height() >= 0) {
                                                        PointF(text.first.x - it.width(), text.first.y - it.height())
                                                    } else {
                                                        PointF(text.first.x, text.first.y - it.height())
                                                    }
                                                }
                                                else -> throw IllegalArgumentException("Unexpected number of points: ${points.size}")
                                            }
                                        }
                                        drawText(text.second, textLocation.x, textLocation.y, store.stringPaint)
                                    }
                                }
                            },
                            null,
                            stack.canUndo(),
                            stack.canRedo()
                    ))
                    createDrawingBase(store)?.run {

                    }
                }
            }
        }
    }

    fun undoDrawing(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            val stack = stacks[store.layoutPosition]
            if (stack.canUndo()) {
                runOnCompute {
                    dispatch(ImageCanvasModel(stack.undo(), null, stack.canUndo(), stack.canRedo()))
                }
            }
        }
    }

    fun redoDrawing(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            val stack = stacks[store.layoutPosition]
            if (stack.canRedo()) {
                runOnCompute {
                    dispatch(ImageCanvasModel(stack.redo(), null, stack.canUndo(), stack.canRedo()))
                }
            }
        }
    }

    fun clearDrawing(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnCompute {
                stacks[store.layoutPosition].reset()
                dispatch(ClearMeasure())
            }
        }
    }

    private fun urisInRange(patientSeriesModel: PatientSeriesModel, start: Int, end: Int)
            = patientSeriesModel.frames.subList(Math.max(0, start), Math.min(end + 1, patientSeriesModel.frames.size)).map { it.frame }

    private fun dispatchShowImage(modId: String, index: Int, dispatch: (Any) -> Unit) {
        seriesModels.find { it.modId == modId }?.let {
            findImage(it, index)?.run {
                dispatch(ShowImage(this, index, it.frames[index].meta))
                bitmapCache.preload(*urisInRange(it, index - preloadRange, index + preloadRange).toTypedArray())
            }
        }
    }

    private fun findImage(model: PatientSeriesModel, index: Int = 0): Bitmap? {
        return when {
            model.frames.isEmpty() || index !in (0..(model.frames.size - 1)) -> null
            else -> loadImage(model.frames[index].frame)
        }
    }

    private fun findFrames(model: PatientSeriesModel, index: Int = 0): List<Bitmap> {
        return when {
            model.frames.isEmpty() || index !in (0..(model.frames.size - 1)) -> emptyList()
            else -> model.frames.subList(index, model.frames.size).mapNotNull { loadImage(it.frame) }
        }
    }

    private fun createDrawingBase(store: ImageFrameStore): Bitmap? {
        return getCurrentImage(store)?.let { Bitmap.createBitmap(it.width, it.height, it.config) }
    }

    private fun getCurrentImage(store: ImageFrameStore): Bitmap? {
        return if (store.displayModel.images.isNotEmpty()) {
            val rawBitmap = store.displayModel.images[0]
            return if (store.autoScale > 1.0f) {
                val newWidth = (rawBitmap.width * store.autoScale).toInt()
                val newHeight = (rawBitmap.height * store.autoScale).toInt()
                Bitmap.createScaledBitmap(rawBitmap, newWidth, newHeight, false)
            } else {
                rawBitmap
            }
        } else {
            null
        }
    }

    private fun cleanMeasure(store: ImageFrameStore, dispatch: (Any) -> Unit) {
        if (store.measure != Measure.NONE) {
            dispatch(ResetMeasure())
            stacks[store.layoutPosition].reset()
        }
    }

    private fun loadImage(uri: URI): Bitmap? {
        return bitmapCache.load(uri)
    }

    private fun toLines(vararg points: PointF): FloatArray {
        return when (points.size) {
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
    }
}