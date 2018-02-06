package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Bitmap
import com.github.charleslzq.kotlin.react.Store
import com.github.charleslzq.pacsdemo.support.callOnIo
import com.github.charleslzq.pacsdemo.support.debugLog

/**
 * Created by charleslzq on 17-11-27.
 * 缩略图数据
 * @param modId dicom数据模型id
 * @param thumb 对应的缩略图
 */
data class ImageThumbModel(val modId: String, val thumb: Bitmap)

/**
 * 主store
 */
class PacsStore : Store<PacsStore>(debugLog, buildThunk<PacsStore>()) {
    /**
     * 缩略图数据
     */
    var thumbList by StoreField(mutableListOf<ImageThumbModel>()) {
        on<SeriesListUpdated> { event.thumbList.toMutableList() }
    }
        private set

    /**
     * 被选中的缩略图的序号, 仅在1*1布局下有用
     */
    var selected: Int by StoreField(-1) {
        on<ChangeLayout> { -1 }
        on<SeriesListUpdated> { -1 }
        on<ThumbListItemClicked>(require = { layoutOption == LayoutOption.ONE_ONE }) {
            event.position
        }
    }
        private set

    /**
     * 布局
     */
    var layoutOption: LayoutOption by StoreField(LayoutOption.ONE_ONE) {
        on<ChangeLayout> { LayoutOption.values()[event.layoutOrdinal] }
    }
        private set

    /**
     * 单元格store列表
     */
    val imageCells: List<ImageFrameStore> = callOnIo { (0..8).map { ImageFrameStore(it) } }
    /**
     * 第一个单元格store
     */
    val firstCell
        get() = imageCells.first()

    enum class LayoutOption {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }

    /**
     * dicom数据模型列表更新事件
     */
    data class SeriesListUpdated(val thumbList: List<ImageThumbModel>)

    /**
     * 布局更改事件
     */
    data class ChangeLayout(val layoutOrdinal: Int)

    /**
     * 缩略图点击事件
     */
    data class ThumbListItemClicked(val position: Int)
}