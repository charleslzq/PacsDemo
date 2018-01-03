package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageRightTopPanel(
        view: View,
        imageFrameStore: ImageFrameStore
) : Component<View, ImageFrameStore>(view, imageFrameStore) {
    private val patientName: TextView = view.findViewById(R.id.patientName)
    private val patientId: TextView = view.findViewById(R.id.patientId)
    private val patientInfo: TextView = view.findViewById(R.id.patientInfo)
    private val institutionName: TextView = view.findViewById(R.id.institutionName)
    private val description: TextView = view.findViewById(R.id.description)

    init {
        render(store::patientMeta) {
            val visible = View.GONE
            patientName.visibility = visible
            patientId.visibility = visible
            patientInfo.visibility = visible
            institutionName.visibility = visible

            it.name?.let {
                patientName.post { patientName.text = it }
                patientName.visibility = View.VISIBLE
            }
            it.id?.let {
                patientId.post { patientId.text = it }
                patientId.visibility = View.VISIBLE
            }

            if (it.birthday != null && it.sex != null) {
                patientInfo.post {
                    patientInfo.text = buildString {
                        append(it.birthday!!)
                        append(" ")
                        append(it.sex!!)
                    }
                }
                patientInfo.visibility = View.VISIBLE
            }

            it.institutionName?.let {
                institutionName.post { institutionName.text = it }
                institutionName.visibility = View.VISIBLE
            }
        }

        render(store::seriesMeta) {
            description.visibility = View.GONE
            it.description?.let {
                description.post { description.text = it }
                description.visibility = View.VISIBLE
            }
        }

        renderByAll(store::hideMeta, store::measure) {
            view.visibility = if (store.hideMeta || store.measure != ImageFrameStore.Measure.NONE) View.INVISIBLE else View.VISIBLE
        }
    }
}