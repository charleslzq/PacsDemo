package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesStore

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageLeftTopPanel(
        panelView: View,
        patientSeriesStore: PatientSeriesStore
) : Component<View, PatientSeriesStore>(panelView, patientSeriesStore) {
    private val windowInfo: TextView = view.findViewById(R.id.windowInfo)
    private val sliceInfo: TextView = view.findViewById(R.id.sliceInfo)
    private val scaleInfo: TextView = view.findViewById(R.id.scaleInfo)
    private val imageProgress: TextView = view.findViewById(R.id.imageProgress)

    init {
        renderByAll(store.imageFramesStore::imageDisplayModel, store.imageFramesStore::imageFramesModel) {
            windowInfo.visibility = View.GONE
            sliceInfo.visibility = View.GONE
            scaleInfo.visibility = View.GONE
            imageProgress.visibility = View.GONE

            store.imageFramesStore.getCurrentFrameMeta()?.let {
                if (it.windowCenter != null && it.windowWidth != null) {
                    windowInfo.post { windowInfo.text = "窗宽: ${it.windowWidth!!} 窗位: ${it.windowCenter!!}" }
                    windowInfo.visibility = View.VISIBLE
                }

                if (it.sliceLocation != null && it.sliceThickness != null) {
                    sliceInfo.post { sliceInfo.text = "T:${it.sliceThickness}mm L:${it.sliceLocation}mm" }
                    sliceInfo.visibility = View.VISIBLE
                }

                scaleInfo.visibility = View.VISIBLE
                imageProgress.visibility = View.VISIBLE
                scaleInfo.post { scaleInfo.text = "缩放: ${store.imageFramesStore.scaleFactor * store.imageFramesStore.rawScale}倍" }
                imageProgress.post { imageProgress.text = "IMAGE: ${store.imageFramesStore.currentIndex() + 1}/${store.imageFramesStore.framesSize()}" }
            }
        }
        render(property = store.imageFramesStore::hideMeta, guard = { store.imageFramesStore.hasImage() }) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }
}