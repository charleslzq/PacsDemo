package com.github.charleslzq.pacsdemo.binder

import ItemClickSupport
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.charleslzq.pacsdemo.DicomSeriesThumbListAdpater
import com.github.charleslzq.pacsdemo.vo.PacsDemoViewModel
import com.github.charleslzq.pacsdemo.vo.PatientSeriesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ThumbListViewBinder(
        recyclerView: RecyclerView
) : ViewBinder<RecyclerView, PacsDemoViewModel>(recyclerView) {

    init {
        view.adapter = DicomSeriesThumbListAdpater(emptyList<PatientSeriesViewModel>().toMutableList())
        view.layoutManager = LinearLayoutManager(recyclerView.context)
        onNewModel { newModel ->
            when (newModel != null && newModel.seriesList.isNotEmpty()) {
                true -> {
                    view.adapter = DicomSeriesThumbListAdpater(newModel!!.seriesList)
                    ItemClickSupport.addTo(view).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                        override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                            newModel.selected = position
                        }
                    })

                    onModelChange(newModel::selected) { oldSelected, newSelected ->
                        if (oldSelected >= 0 && oldSelected <= view.childCount) {
                            view.getChildAt(oldSelected).isSelected = false
                        }
                        if (newSelected >= 0 && newSelected < view.childCount) {
                            view.getChildAt(newSelected).isSelected = true
                        }
                    }
                }
                false -> {
                    view.adapter = DicomSeriesThumbListAdpater(emptyList<PatientSeriesViewModel>().toMutableList())
                }
            }
            view.adapter!!.notifyDataSetChanged()
        }
    }
}