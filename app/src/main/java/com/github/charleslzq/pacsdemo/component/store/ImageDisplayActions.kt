package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import com.github.charleslzq.kotlin.react.DispatchAction
import com.github.charleslzq.pacsdemo.support.BitmapCache
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import java.net.URI

/**
 * Created by charleslzq on 18-1-12.
 */
data class PatientSeriesModel(
    val modId: String = "",
    val patientMetaInfo: DicomPatientMetaInfo = DicomPatientMetaInfo(),
    val studyMetaInfo: DicomStudyMetaInfo = DicomStudyMetaInfo(),
    val seriesMetaInfo: DicomSeriesMetaInfo = DicomSeriesMetaInfo(),
    val frames: List<ImageFrameModel> = emptyList(),
    val thumb: URI? = null
)

object ImageDisplayActions : RxScheduleSupport {
    private val seriesModels: MutableList<PatientSeriesModel> = mutableListOf()
    private var bitmapCache = BitmapCache()
    private const val PRE_LOAD_RANGE = 5

    fun reloadModels(patientSeriesModelList: List<PatientSeriesModel>): DispatchAction<PacsStore> {
        seriesModels.clear()
        seriesModels.addAll(patientSeriesModelList)
        val thumbList = callOnIo {
            seriesModels.filter { it.modId.isNotBlank() && it.thumb != null }
                .mapNotNull {
                    BitmapCache.decode(it.thumb!!)
                        ?.let { thumb -> ImageThumbModel(it.modId, thumb) }
                }
        }
        return { store, dispatch, _ ->
            store.dispatch(changeLayout(PacsStore.LayoutOption.ONE_ONE))
            dispatch(PacsStore.SeriesListUpdated(thumbList))
        }
    }

    fun changeLayout(layoutOrdinal: Int): DispatchAction<PacsStore> = { store, _, _ ->
        val ordinal = layoutOrdinal.rem(PacsStore.LayoutOption.values().size)
            .let { if (it < 0) it + PacsStore.LayoutOption.values().size else it }
        store.dispatch(changeLayout(PacsStore.LayoutOption.values()[ordinal]))
    }

    fun changeLayout(layoutOption: PacsStore.LayoutOption): DispatchAction<PacsStore> =
        { store, dispatch, _ ->
            if (store.layoutOption != layoutOption) {
                dispatch(PacsStore.ChangeLayout(layoutOption.ordinal))
                store.imageCells.forEach {
                    it.dispatch(ImageFrameStore.Reset())
                    it.dispatch(
                        if (layoutOption == PacsStore.LayoutOption.ONE_ONE) {
                            ImageFrameStore.AllowPlay()
                        } else {
                            ImageFrameStore.ForbidPlay()
                        }
                    )
                }
                bitmapCache = BitmapCache(100)
            }
        }

    fun bindModel(modId: String, index: Int = 0): DispatchAction<ImageFrameStore> =
        { store, dispatch, _ ->
            runOnIo {
                store.dispatch(ImageMeasureActions.clearDrawing())
                seriesModels.find { it.modId == modId }?.let {
                    dispatch(
                        ImageFrameStore.BindModel(
                            modId,
                            it.patientMetaInfo,
                            it.studyMetaInfo,
                            it.seriesMetaInfo,
                            it.frames.size
                        )
                    )
                    findImage(it, index)?.run {
                        dispatch(ImageFrameStore.ShowImage(this, index, it.frames[index].meta))
                    }
                    if (store.playable) {
                        bitmapCache = BitmapCache(Math.max(100, it.frames.size))
                        bitmapCache.preload(*it.frames.map { it.frame }.toTypedArray())
                    } else {
                        bitmapCache.preload(
                            *urisInRange(
                                it,
                                index - PRE_LOAD_RANGE,
                                index + PRE_LOAD_RANGE
                            ).toTypedArray()
                        )
                    }
                }
            }
        }

    fun moveFrame(imageFrameStore: ImageFrameStore): DispatchAction<ImageFrameStore> =
        { store, dispatch, _ ->
            runOnCompute {
                store.dispatch(ImageMeasureActions.clearDrawing())
                seriesModels.find { it.modId == imageFrameStore.bindModId }!!.let {
                    findImage(it, imageFrameStore.index)!!.run {
                        dispatch(
                            ImageFrameStore.MoveModel(
                                it.modId,
                                it.patientMetaInfo,
                                it.studyMetaInfo,
                                it.seriesMetaInfo,
                                it.frames.size,
                                this,
                                imageFrameStore.index,
                                it.frames[imageFrameStore.index].meta,
                                imageFrameStore.reverseColor,
                                imageFrameStore.pseudoColor,
                                imageFrameStore.canvasModel
                            )
                        )
                    }
                }

                store.dispatch(ImageMeasureActions.moveStackFrom(imageFrameStore))
                imageFrameStore.dispatch(ImageFrameStore.Reset())
            }
        }

    fun playOrPause(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        if (store.playable) {
            runOnIo {
                store.dispatch(ImageMeasureActions.clearDrawing())
                if (store.displayModel.images.size > 1 && store.autoJumpIndex != 0) {
                    dispatchShowImage(store.bindModId, store.index, dispatch)
                } else {
                    seriesModels.find { it.modId == store.bindModId }?.let {
                        dispatch(ImageFrameStore.PlayAnimation(findFrames(it, store.autoJumpIndex)))
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
                dispatch(ImageFrameStore.PlayIndexChange(index, it.frames[index].meta))
            }
        }
    }

    fun indexScroll(scrollDistance: Float): DispatchAction<ImageFrameStore> =
        { store, dispatch, _ ->
            runOnIo {
                if (store.size > 0) {
                    store.dispatch(ImageMeasureActions.clearDrawing())
                    val changeBase = Math.min(300f / store.size, 10f)
                    val offset = (scrollDistance / changeBase).toInt()
                    val newIndex = Math.min(Math.max(store.index - offset, 0), store.size - 1)
                    dispatchShowImage(store.bindModId, newIndex, dispatch)
                }
            }
        }

    fun resetDisplay(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnIo {
            store.dispatch(ImageMeasureActions.clearDrawing())
            dispatch(ImageFrameStore.ResetDisplay())
            if (store.size > 1) {
                dispatchShowImage(store.bindModId, 0, dispatch)
            }
        }
    }

    private fun urisInRange(patientSeriesModel: PatientSeriesModel, start: Int, end: Int) =
        patientSeriesModel.frames.subList(
            Math.max(0, start),
            Math.min(end + 1, patientSeriesModel.frames.size)
        ).map { it.frame }

    private fun dispatchShowImage(modId: String, index: Int, dispatch: (Any) -> Unit) =
        seriesModels.find { it.modId == modId }?.let {
            findImage(it, index)?.run {
                dispatch(ImageFrameStore.ShowImage(this, index, it.frames[index].meta))
                bitmapCache.preload(
                    *urisInRange(
                        it,
                        index - PRE_LOAD_RANGE,
                        index + PRE_LOAD_RANGE
                    ).toTypedArray()
                )
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

    private fun loadImage(uri: URI) = bitmapCache.load(uri)
}