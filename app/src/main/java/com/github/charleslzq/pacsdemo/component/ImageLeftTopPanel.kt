package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageLeftTopPanel(
        panelView: View,
        imageFrameStore: ImageFrameStore
) : Component<View, ImageFrameStore>(panelView, imageFrameStore) {
    private val windowInfo: TextView = view.findViewById(R.id.windowInfo)
    private val sliceInfo: TextView = view.findViewById(R.id.sliceInfo)
    private val scaleInfo: TextView = view.findViewById(R.id.scaleInfo)
    private val imageProgress: TextView = view.findViewById(R.id.imageProgress)

    init {
        render(store::imageMeta) {
            windowInfo.visibility = View.GONE
            sliceInfo.visibility = View.GONE

            if (it.windowCenter != null && it.windowWidth != null) {
                windowInfo.post { windowInfo.text = "窗宽: ${it.windowWidth!!} 窗位: ${it.windowCenter!!}" }
                windowInfo.visibility = View.VISIBLE
            }

            if (it.sliceLocation != null && it.sliceThickness != null) {
                sliceInfo.post { sliceInfo.text = "T:${it.sliceThickness}mm L:${it.sliceLocation}mm" }
                sliceInfo.visibility = View.VISIBLE
            }
        }

        renderByAll(store::autoScale, store::gestureScale) {
            if (store.hasImage) {
                scaleInfo.visibility = View.VISIBLE
                scaleInfo.post { scaleInfo.text = "缩放: ${store.scale}倍" }
            } else {
                scaleInfo.visibility = View.GONE
            }
        }

        renderByAll(store::size, store::index) {
            if (store.hasImage) {
                imageProgress.visibility = View.VISIBLE
                imageProgress.post { imageProgress.text = "IMAGE: ${store.index + 1}/${store.size}" }
            } else {
                imageProgress.visibility = View.GONE
            }
        }

        renderByAll(store::hideMeta, store::measure) {
            view.visibility = if (store.hideMeta || store.measure != ImageFrameStore.Measure.NONE) View.INVISIBLE else View.VISIBLE
        }
    }
}