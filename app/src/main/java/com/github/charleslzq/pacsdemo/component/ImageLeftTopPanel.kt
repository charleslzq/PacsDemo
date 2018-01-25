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

            if (it.windowCenter.isNotBlank() && it.windowWidth.isNotBlank()) {
                windowInfo.text = windowInfo.resources.getString(
                    R.string.window_info,
                    it.windowCenter,
                    it.windowWidth
                )
                windowInfo.visibility = View.VISIBLE
            }

            if (it.sliceLocation > 0 && it.sliceThickness > 0) {
                sliceInfo.text = sliceInfo.resources.getString(
                    R.string.slice_info,
                    it.sliceLocation,
                    it.sliceThickness
                )
                sliceInfo.visibility = View.VISIBLE
            }
        }

        renderByAll(store::autoScale, store::gestureScale) {
            if (store.hasImage) {
                scaleInfo.text = scaleInfo.resources.getString(R.string.scale_info, store.scale)
                scaleInfo.visibility = View.VISIBLE
            } else {
                scaleInfo.visibility = View.GONE
            }
        }

        renderByAll(store::size, store::index) {
            if (store.hasImage) {
                imageProgress.visibility = View.VISIBLE
                imageProgress.text = imageProgress.resources.getString(
                    R.string.image_progress,
                    store.index + 1,
                    store.size
                )
            } else {
                imageProgress.visibility = View.GONE
            }
        }

        renderByAll(store::hideMeta, store::measure) {
            view.visibility =
                    if (store.hideMeta || store.measure != ImageFrameStore.Measure.NONE) View.INVISIBLE else View.VISIBLE
        }
    }
}