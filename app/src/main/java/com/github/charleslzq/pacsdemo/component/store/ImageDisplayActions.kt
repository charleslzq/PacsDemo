package com.github.charleslzq.pacsdemo.component.store

import com.github.charleslzq.dicom.data.DicomPatientMetaInfo
import com.github.charleslzq.dicom.data.DicomSeriesMetaInfo
import com.github.charleslzq.dicom.data.DicomStudyMetaInfo
import com.github.charleslzq.kotlin.react.DispatchAction
import com.github.charleslzq.pacsdemo.support.BitmapCache
import com.github.charleslzq.pacsdemo.support.callOnIo
import com.github.charleslzq.pacsdemo.support.runOnCompute
import com.github.charleslzq.pacsdemo.support.runOnIo
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

object ImageDisplayActions {
    private val seriesModels: MutableMap<String, PatientSeriesModel> = mutableMapOf()
    private var bitmapCache = BitmapCache()
    private const val PRE_LOAD_RANGE = 5

    /**
     * @return 更新dicom数据模型列表操作, 会将布局调整至1*1并更新缩略图列表
     */
    fun reloadModels(patientSeriesModelList: List<PatientSeriesModel>): DispatchAction<PacsStore> {
        seriesModels.clear()
        patientSeriesModelList.forEach { seriesModels[it.modId] = it }
        val thumbList = callOnIo {
            seriesModels.filter { it.value.thumb != null }
                .mapNotNull {
                    BitmapCache.decode(it.value.thumb!!)
                        ?.let { thumb -> ImageThumbModel(it.key, thumb) }
                }
        }
        return { store, dispatch, _ ->
            store.dispatch(changeLayout(PacsStore.LayoutOption.ONE_ONE))
            dispatch(PacsStore.SeriesListUpdated(thumbList))
        }
    }

    /**
     * @return 更改布局操作
     */
    fun changeLayout(layoutOrdinal: Int): DispatchAction<PacsStore> = { store, _, _ ->
        val ordinal = layoutOrdinal.rem(PacsStore.LayoutOption.values().size)
            .let { if (it < 0) it + PacsStore.LayoutOption.values().size else it }
        store.dispatch(changeLayout(PacsStore.LayoutOption.values()[ordinal]))
    }

    /**
     * @return 更改布局操作
     */
    fun changeLayout(layoutOption: PacsStore.LayoutOption): DispatchAction<PacsStore> =
        { store, dispatch, _ ->
            if (store.layoutOption != layoutOption) {
                dispatch(PacsStore.ChangeLayout(layoutOption.ordinal))
                store.imageCells.forEach {
                    it.dispatch(ImageFrameStore.Reset())
                    it.dispatch(ImageFrameStore.SetAllowPlay(layoutOption == PacsStore.LayoutOption.ONE_ONE))
                }
                bitmapCache = BitmapCache(100)
            }
        }

    /**
     * @return 绑定模型到某个单元格的操作
     */
    fun bindModel(modId: String, index: Int = 0): DispatchAction<ImageFrameStore> =
        { store, dispatch, _ ->
            runOnIo {
                store.dispatch(ImageMeasureActions.clearDrawing())
                seriesModels[modId]?.let {
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

    /**
     * @return 移动单元格数据到另一个单元格的操作
     */
    fun moveFrame(imageFrameStore: ImageFrameStore): DispatchAction<ImageFrameStore> =
        { store, dispatch, _ ->
            runOnCompute {
                store.dispatch(ImageMeasureActions.clearDrawing())
                seriesModels[imageFrameStore.bindModId]!!.let {
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

    /**
     * @return 播放或者暂停的操作(取决于当前处于播放还是暂停状态)
     */
    fun playOrPause(): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        if (store.playable) {
            runOnIo {
                store.dispatch(ImageMeasureActions.clearDrawing())
                if (store.displayModel.images.size > 1 && store.autoJumpIndex != 0) {
                    dispatchShowImage(store.bindModId, store.index, dispatch)
                } else {
                    seriesModels[store.bindModId]?.let {
                        dispatch(ImageFrameStore.PlayAnimation(findFrames(it, store.autoJumpIndex)))
                    }
                }
            }
        }
    }

    /**
     * @return 显示指定序号图像的操作
     */
    fun showImage(index: Int): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        runOnIo {
            dispatchShowImage(store.bindModId, index, dispatch)
        }
    }

    /**
     * @return 更新图像序号和元信息的操作
     */
    fun playIndexChange(index: Int): DispatchAction<ImageFrameStore> = { store, dispatch, _ ->
        seriesModels[store.bindModId]?.let {
            if (index in 0 until it.frames.size) {
                dispatch(ImageFrameStore.PlayIndexChange(index, it.frames[index].meta))
            }
        }
    }

    /**
     * @return 响应手指滑动显示图像的操作
     */
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

    /**
     * @return 重置图像显示状态的操作
     */
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
        seriesModels[modId]?.let {
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

    /**
     * 在数据模型中寻找指定序号的图像
     */
    private fun findImage(model: PatientSeriesModel, index: Int = 0) = when {
        model.frames.isEmpty() || index !in (0 until model.frames.size) -> null
        else -> loadImage(model.frames[index].frame)
    }

    /**
     * 在数据模型中, 返回指定序号的图像及其之后的图像的列表,用于动画播放
     */
    private fun findFrames(model: PatientSeriesModel, index: Int = 0) = when {
        model.frames.isEmpty() || index !in (0 until model.frames.size) -> emptyList()
        else -> model.frames.subList(index, model.frames.size).mapNotNull { loadImage(it.frame) }
    }

    private fun loadImage(uri: URI) = bitmapCache.load(uri)
}