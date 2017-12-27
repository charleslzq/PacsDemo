package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesStore

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageRightTopPanel(
        view: View,
        patientSeriesStore: PatientSeriesStore
) : Component<View, PatientSeriesStore>(view, patientSeriesStore) {
    private val patientName: TextView = view.findViewById(R.id.patientName)
    private val patientId: TextView = view.findViewById(R.id.patientId)
    private val patientInfo: TextView = view.findViewById(R.id.patientInfo)
    private val institutionName: TextView = view.findViewById(R.id.institutionName)
    private val description: TextView = view.findViewById(R.id.description)

    init {
        render(store::patientSeriesModel) {
            val visible = View.GONE
            patientName.visibility = visible
            patientId.visibility = visible
            patientInfo.visibility = visible
            institutionName.visibility = visible
            description.visibility = visible

            if (store.imageFramesStore.hasImage()) {
                val meta = it.patientMetaInfo
                meta.name?.let {
                    patientName.post { patientName.text = it }
                    patientName.visibility = View.VISIBLE
                }
                meta.id?.let {
                    patientId.post { patientId.text = it }
                    patientId.visibility = View.VISIBLE
                }

                if (meta.birthday != null && meta.sex != null) {
                    patientInfo.post {
                        patientInfo.text = buildString {
                            append(meta.birthday!!)
                            append(" ")
                            append(meta.sex!!)
                        }
                    }
                    patientInfo.visibility = View.VISIBLE
                }

                meta.institutionName?.let {
                    institutionName.post { institutionName.text = it }
                    institutionName.visibility = View.VISIBLE
                }

                it.dicomSeriesMetaInfo.description?.let {
                    description.post { description.text = it }
                    description.visibility = View.VISIBLE
                }
            }
        }

        render(property = store::hideMeta, guard = { store.imageFramesStore.hasImage() }) {
            view.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }
}