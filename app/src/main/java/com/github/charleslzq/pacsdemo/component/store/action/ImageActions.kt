package com.github.charleslzq.pacsdemo.component.store.action

import android.graphics.Bitmap
import com.github.charleslzq.kotlin.react.DispatchAction
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.*
import com.github.charleslzq.pacsdemo.component.store.ImageThumbModel
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesModel
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import java.net.URI

/**
 * Created by charleslzq on 17-12-28.
 */
object ImageActions : RxScheduleSupport {
    private val seriesModels: MutableList<PatientSeriesModel> = mutableListOf()
    private var bitmapCache = BitmapCache()

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
        return { _, dispatch, _ ->
            runOnIo {
                seriesModels.find { it.modId == modId }?.let {
                    dispatch(BindModel(modId, it.patientMetaInfo, it.studyMetaInfo, it.seriesMetaInfo, it.frames.size))
                    findImage(it, index)?.run {
                        dispatch(ShowImage(this, index, it.frames[index].meta))
                    }
                }
            }
        }
    }

    fun playOrPause(): DispatchAction<ImageFrameStore> {
        return { store, dispatch, _ ->
            if (store.playable()) {
                runOnIo {
                    val index = store.index
                    if (store.imageDisplayModel.images.size > 1) {
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
                if (index in (0..(it.frames.size-1))) {
                    dispatch(PlayIndexChange(index, it.frames[index].meta))
                }
            }
        }
    }

    private fun dispatchShowImage(modId: String, index: Int, dispatch: (Any) -> Unit) {
        seriesModels.find { it.modId == modId }?.let {
            findImage(it, index)?.run {
                dispatch(ShowImage(this, index, it.frames[index].meta))
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

    private fun loadImage(uri: URI): Bitmap? {
        return BitmapCache.decode(uri)
    }
}