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

    fun bindModel(modId: String, index: Int = 0): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                cleanMeasure(store, dispatch)

                seriesModels.find { it.modId == modId }?.let {
                    dispatch(BindModel(modId, it.patientMetaInfo, it.studyMetaInfo, it.seriesMetaInfo, it.frames.size))
                    findImage(it, index)?.run {
                        dispatch(ShowImage(this, index, it.frames[index].meta))
                    }
                    if (store.allowPlay) {
                        bitmapCache = BitmapCache(Math.max(100, it.frames.size))
                        bitmapCache.preload(*it.frames.map { it.frame }.toTypedArray())
                    } else {
                        bitmapCache.preload(*urisInRange(it, 0, 2 * preloadRange).toTypedArray())
                    }
                }
            }
        }
    }

    fun playOrPause(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            if (store.playable()) {
                runOnIo {
                    cleanMeasure(store, dispatch)

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
                cleanMeasure(store, dispatch)

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
                    cleanMeasure(store, dispatch)

                    val changeBase = Math.min(100f / store.size, 10f)
                    val offset = (scrollDistance / changeBase).toInt()
                    val newIndex = Math.min(Math.max(store.index - offset, 0), store.size - 1)
                    dispatchShowImage(store.bindModId, newIndex, dispatch)
                }
            }
        }
    }

    fun pseudoColor() : DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                dispatch(PseudoColor())
                dispatchShowImage(store.bindModId, store.index, dispatch)
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

    fun addPath(points: List<PointF>, text: Pair<PointF, String>): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            runOnIo {
                val stack = stacks[store.layoutPosition]
                if (!stack.initialized()) {
                    seriesModels.find { it.modId == store.bindModId }?.let {
                        findImage(it, store.index)?.let {
                            Bitmap.createBitmap(it.width, it.height, it.config).let { stack.done(it) }
                        }
                    }
                }
                if (stack.initialized() && points.size > 1) {
                    dispatch(ImageCanvasModel(
                            stack.generate {
                                Bitmap.createBitmap(it.width, it.height, it.config).apply {
                                    Canvas(this).apply {
                                        drawBitmap(it, 0f, 0f, store.linePaint)
                                        drawPath(Path().apply {
                                            moveTo(points.first().x, points.first().y)
                                            repeat(points.size - 2) {
                                                lineTo(points[it + 1].x, points[it + 1].y)
                                            }
                                        }, store.linePaint)
                                        drawText(text.second, text.first.x, text.first.y, store.stringPaint)
                                    }
                                }
                            },
                            emptyList(),
                            stack.canUndo(),
                            stack.canRedo()
                    ))
                }
            }
        }
    }

    fun undoDrawing(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            val stack = stacks[store.layoutPosition]
            if (stack.canUndo()) {
                runOnCompute {
                    dispatch(stack.undo())
                }
            }
        }
    }

    fun redoDrawing(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            val stack = stacks[store.layoutPosition]
            if (stack.canRedo()) {
                runOnCompute {
                    dispatch(stack.redo())
                }
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

    private fun cleanMeasure(store: ImageFrameStore, dispatch: (Any) -> Unit) {
        if (store.measure != Measure.NONE) {
            dispatch(ResetMeasure())
            stacks[store.layoutPosition].reset()
        }
    }

    private fun loadImage(uri: URI): Bitmap? {
        return bitmapCache.load(uri)
    }
}