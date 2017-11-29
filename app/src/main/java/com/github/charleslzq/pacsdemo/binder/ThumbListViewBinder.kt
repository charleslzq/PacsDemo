package com.github.charleslzq.pacsdemo.binder

import ItemClickSupport
import android.content.ClipData
import android.content.ClipDescription
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.charleslzq.pacsdemo.DicomSeriesThumbListAdpater
import com.github.charleslzq.pacsdemo.binder.vo.PacsDemoViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ThumbListViewBinder(
        recyclerView: RecyclerView
) : ViewBinder<RecyclerView, PacsDemoViewModel>(recyclerView, { PacsDemoViewModel() }) {
    private val tag = "thumbList"

    init {
        view.layoutManager = LinearLayoutManager(recyclerView.context)
        onNewModel {
            view.adapter = DicomSeriesThumbListAdpater(model.seriesList)
            if (model.seriesList.isNotEmpty()) {
                ItemClickSupport.addTo(view).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                        model.selected = position
                    }
                })
                ItemClickSupport.addTo(view).setOnItemLongClickListener(object : ItemClickSupport.OnItemLongClickListener {
                    @Suppress("DEPRECATION")
                    override fun onItemLongClicked(recyclerView: RecyclerView, position: Int, v: View): Boolean {
                        if (position >= 0 && position < view.childCount) {
                            val targetView = view.getChildAt(position)
                            val dragBuilder = View.DragShadowBuilder(targetView)
                            val clipDataItem = ClipData.Item(tag, position.toString())
                            val clipData = ClipData(tag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), clipDataItem)
                            targetView.startDrag(clipData, dragBuilder, null, 0)
                        }
                        return true
                    }
                })

                onModelChange(model::selected) {
                    if (it.first >= 0 && it.first < view.childCount) {
                        view.getChildAt(it.first).isSelected = false
                    }
                    if (it.second >= 0 && it.second < view.childCount) {
                        view.getChildAt(it.second).isSelected = true
                    }
                }
            }
            view.invalidate()
        }
    }
}