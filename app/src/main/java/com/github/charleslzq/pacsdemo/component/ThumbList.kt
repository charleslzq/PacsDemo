package com.github.charleslzq.pacsdemo.component

import ItemClickSupport
import android.content.ClipData
import android.content.ClipDescription
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.charleslzq.pacsdemo.DicomSeriesThumbListAdpater
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class ThumbList(
        recyclerView: RecyclerView,
        pacsViewState: PacsViewState
) : PacsComponent<RecyclerView>(recyclerView, pacsViewState) {
    private val tag = "thumbList"

    init {
        view.layoutManager = LinearLayoutManager(recyclerView.context)
        onStateChange(state::seriesList) {
            val adapter = view.adapter
            if (adapter != null && adapter is DicomSeriesThumbListAdpater) {
                adapter.seriesModels.clear()
                adapter.seriesModels.addAll(state.seriesList)
            } else {
                view.adapter = DicomSeriesThumbListAdpater(state.seriesList)
            }
            view.adapter.notifyDataSetChanged()
            if (state.seriesList.isNotEmpty()) {
                ItemClickSupport.addTo(view).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                        state.selected = position
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
            }
        }

        onStateChange(state::selected) {
            if (it.first >= 0 && it.first < view.childCount) {
                view.getChildAt(it.first).isSelected = false
            }
            if (it.second >= 0 && it.second < view.childCount) {
                view.getChildAt(it.second).isSelected = true
            }
        }
    }
}