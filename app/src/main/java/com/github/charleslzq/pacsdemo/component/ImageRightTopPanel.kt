package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.base.Component
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesViewState

/**
 * Created by charleslzq on 17-12-6.
 */
class ImageRightTopPanel(
        view: View,
        patientSeriesViewState: PatientSeriesViewState
) : Component<View, PatientSeriesViewState>(view, patientSeriesViewState) {
    private val patientName: TextView = view.findViewById(R.id.patientName)
    private val patientId: TextView = view.findViewById(R.id.patientId)
    private val patientInfo: TextView = view.findViewById(R.id.patientInfo)
    private val institutionName: TextView = view.findViewById(R.id.institutionName)
    private val description: TextView = view.findViewById(R.id.description)

    init {
        onStateChange(state::patientSeriesModel) {
            if (state.patientSeriesModel.imageFramesModel.size > 0) {
                val name = state.patientSeriesModel.patientMetaInfo.name ?: "UNKNOWN"
                val id = state.patientSeriesModel.patientMetaInfo.id ?: "UNKNOWN"
                val birthday = state.patientSeriesModel.patientMetaInfo.birthday ?: "UNKNOWN"
                val gender = state.patientSeriesModel.patientMetaInfo.sex ?: "UNKNOWN"
                val institution = state.patientSeriesModel.patientMetaInfo.institutionName ?: "UNKNOWN"
                val des = state.patientSeriesModel.dicomSeriesMetaInfo.description ?: "UNKNOWN"

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