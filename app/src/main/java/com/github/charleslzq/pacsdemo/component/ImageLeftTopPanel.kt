package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
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
        render(store.imageFramesStore::imagePlayModel) {
            windowInfo.visibility = View.INVISIBLE
            sliceInfo.visibility = View.INVISIBLE
            scaleInfo.visibility = View.INVISIBLE
            imageProgress.visibility = View.INVISIBLE

            store.imageFramesStore.getCurrentFrameMeta()?.let {
                windowInfo.visibility = View.VISIBLE
                sliceInfo.visibility = View.VISIBLE
                scaleInfo.visibility = View.VISIBLE
                imageProgress.visibility = View.VISIBLE

                val windowCenter = it.windowCenter ?: "不明"
                val windowWidth = it.windowWidth ?: "不明"
                windowInfo.post { windowInfo.text = "窗宽: $windowWidth 窗位: $windowCenter" }

                val sliceThickness = it.sliceThickness ?: "不明"
                val sliceLocation = it.sliceLocation ?: "不明"
                sliceInfo.post { sliceInfo.text = "T:${sliceThickness}mm L:${sliceLocation}mm" }

                scaleInfo.post { scaleInfo.text = "缩放: ${store.imageFramesStore.scaleFactor * store.imageFramesStore.rawScale}倍" }

                imageProgress.post { imageProgress.text = "IMAGE: ${store.imageFramesStore.currentIndex() + 1}/${store.imageFramesStore.framesSize()}" }
            }
        }

    }
}