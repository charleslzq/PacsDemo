package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesStore

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageLeftTopPanel(
        panelView: LinearLayout,
        patientSeriesStore: PatientSeriesStore
) : Component<LinearLayout, PatientSeriesStore>(panelView, patientSeriesStore) {
    private val windowInfo: TextView = view.findViewById(R.id.windowInfo)
    private val sliceInfo: TextView = view.findViewById(R.id.sliceInfo)
    private val scaleInfo: TextView = view.findViewById(R.id.scaleInfo)
    private val imageProgress: TextView = view.findViewById(R.id.imageProgress)

    init {
        bind(store.imageFramesStore::imagePlayModel) {
            val visible = when (store.patientSeriesModel.imageFramesModel.size == 0) {
                true -> View.INVISIBLE
                false -> View.VISIBLE
            }
            windowInfo.visibility = visible
            sliceInfo.visibility = visible
            scaleInfo.visibility = visible
            imageProgress.visibility = visible
            if (store.patientSeriesModel.imageFramesModel.size > 0) {
                val imageInfo = store.patientSeriesModel.imageFramesModel.frames[it.currentIndex]

                val windowCenter = imageInfo.windowCenter ?: "不明"
                val windowWidth = imageInfo.windowWidth ?: "不明"
                windowInfo.post { windowInfo.text = "窗宽: $windowWidth 窗位: $windowCenter" }

                val sliceThickness = imageInfo.sliceThickness ?: "不明"
                val sliceLocation = imageInfo.sliceLocation ?: "不明"
                sliceInfo.post { sliceInfo.text = "T:${sliceThickness}mm L:${sliceLocation}mm" }

                scaleInfo.post { scaleInfo.text = "缩放: ${store.imageFramesStore.scaleFactor * store.imageFramesStore.rawScale}倍" }

                imageProgress.post { imageProgress.text = "IMAGE: ${it.currentIndex + 1}/${store.imageFramesStore.framesModel.size}" }
            }
        }

    }
}