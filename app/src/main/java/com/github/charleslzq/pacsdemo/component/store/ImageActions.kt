package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
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
                        dispatch(ShowImage(copy(config, false), index, it.frames[index].meta))
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
                    val index = store.index
                    if (store.displayModel.images.size > 1) {
                        dispatchShowImage(store.bindModId, index, dispatch)
                    } else {
                        seriesModels.find { it.modId == store.bindModId }?.let {
                            dispatch(PlayAnimation(findFrames(it, index)))
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
                if (index in (0..(it.frames.size - 1))) {
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

    fun drawLines(vararg points: PointF): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                val coordinates = toLines(*points)
                if (coordinates.size > 1 && store.hasImage) {
                    createDrawingBase(store)?.let {
                        dispatch(DrawLines(it.apply {
                            Canvas(this).apply {
                                drawCircle(coordinates[coordinates.size - 2], coordinates.last(), 5f, store.pointPaint)
                                if (coordinates.size > 3) {
                                    drawLines(coordinates, store.linePaint)
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
                    createDrawingBase(store)?.run {
                        dispatch(ImageCanvasModel(
                                stack.generate(this) {
                                    Bitmap.createBitmap(it.width, it.height, it.config).apply {
                                        Canvas(this).apply {
                                            drawBitmap(it, 0f, 0f, store.linePaint)
                                            drawPath(Path().apply {
                                                moveTo(points[0].x, points[0].y)
                                                repeat(points.size - 1) {
                                                    lineTo(points[it + 1].x, points[it + 1].y)
                                                }
                                            }, store.linePaint)
                                            drawText(text.second, text.first.x, text.first.y, store.stringPaint)
                                        }
                                    }
                                },
                                null,
                                stack.canUndo(),
                                stack.canRedo()
                        ))
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
                dispatch(ShowImage(copy(config, true), index, it.frames[index].meta))
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
        return if (store.displayModel.images.isNotEmpty()) {
            store.displayModel.images[0].let {
                val scale = Math.max(store.autoScale, 1.0f)
                Bitmap.createBitmap((it.width * scale).toInt(), (it.height * scale).toInt(), it.config)
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