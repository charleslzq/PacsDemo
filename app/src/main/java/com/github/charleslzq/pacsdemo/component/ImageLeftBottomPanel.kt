package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageLeftBottomPanel(
        panelView: View,
        imageFrameStore: ImageFrameStore
) : Component<View, ImageFrameStore>(panelView, imageFrameStore) {
    private val xRayInfo: TextView = view.findViewById(R.id.xRayInfo)
    private val timeInfo: TextView = view.findViewById(R.id.time)

    init {
        render(store::imageMeta) {
            xRayInfo.visibility = View.GONE

            buildString {
                it.xRayTubCurrent?.let { append("$it mA ") }
                it.kvp?.let { append("$it KV") }
            }.takeIf { it.isNotBlank() }?.let {
                xRayInfo.post { xRayInfo.text = it }
                xRayInfo.visibility = View.VISIBLE
            }
        }

        render(store::seriesMeta) {
            timeInfo.visibility = View.GONE

            buildString {
                it.date?.apply { append(this + " ") }
                it.time?.apply { append(this) }
            }.takeIf { it.isNotBlank() }?.let {
                timeInfo.post { timeInfo.text = it }
                timeInfo.visibility = View.VISIBLE
            }
        }

        render(store::hideMeta) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }
}