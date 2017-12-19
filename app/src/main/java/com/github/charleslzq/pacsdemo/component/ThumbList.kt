package com.github.charleslzq.pacsdemo.component

import ItemClickSupport
import android.content.ClipData
import android.content.ClipDescription
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.support.DicomSeriesThumbListAdpater

/**
 * Created by charleslzq on 17-11-27.
 */
class ThumbList(
        recyclerView: RecyclerView,
        pacsStore: PacsStore
) : PacsComponent<RecyclerView>(recyclerView, pacsStore) {

    init {
        view.layoutManager = LinearLayoutManager(recyclerView.context)
        render(store::seriesList) {
            val adapter = view.adapter
            if (adapter != null && adapter is DicomSeriesThumbListAdpater) {
                adapter.seriesModels.clear()
                adapter.seriesModels.addAll(store.seriesList)
            } else {
                view.adapter = DicomSeriesThumbListAdpater(store.seriesList)
            }
            view.adapter.notifyDataSetChanged()
            if (store.seriesList.isNotEmpty()) {
                ItemClickSupport.addTo(view).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                        EventBus.post(ClickEvent.ThumbListItemClicked(position))
                        if (position in (0..(store.seriesList.size - 1))) {
                            EventBus.post(ImageDisplayEvent.PlayModeReset(0))
                            EventBus.post(BindingEvent.ModelSelected(store.seriesList[position]))
                        }
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

                (1..view.childCount).forEach { view.getChildAt(it - 1).isSelected = false }
                if (store.selected >= 0 && store.selected < view.childCount) {
                    view.getChildAt(store.selected).isSelected = true
                }
            }
        }

        render(store::selected) {
            (1..view.childCount).forEach { view.getChildAt(it - 1).isSelected = false }
            if (it >= 0 && it < view.childCount) {
                view.getChildAt(it).isSelected = true
            }
        }
    }

    companion object {
        val tag = "thumbList"
    }
}