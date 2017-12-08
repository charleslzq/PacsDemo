package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.base.Component
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
        refreshByProperty(store::patientSeriesModel) {
            if (store.patientSeriesModel.imageFramesModel.size > 0) {
                val name = store.patientSeriesModel.patientMetaInfo.name ?: "UNKNOWN"
                val id = store.patientSeriesModel.patientMetaInfo.id ?: "UNKNOWN"
                val birthday = store.patientSeriesModel.patientMetaInfo.birthday ?: "UNKNOWN"
                val gender = store.patientSeriesModel.patientMetaInfo.sex ?: "UNKNOWN"
                val institution = store.patientSeriesModel.patientMetaInfo.institutionName ?: "UNKNOWN"
                val des = store.patientSeriesModel.dicomSeriesMetaInfo.description ?: "UNKNOWN"

                patientName.post { patientName.text = name }
                patientId.post { patientId.text = id }
                patientInfo.post { patientInfo.text = "$birthday $gender" }
                institutionName.post { institutionName.text = institution }
                description.post { description.text = des }

                val visible = View.VISIBLE
                patientName.visibility = visible
                patientId.visibility = visible
                patientInfo.visibility = visible
                institutionName.visibility = visible
                description.visibility = visible
            } else {
                val visible = View.INVISIBLE
                patientName.visibility = visible
                patientId.visibility = visible
                patientInfo.visibility = visible
                institutionName.visibility = visible
                description.visibility = visible
            }
        }
    }
}