package com.github.charleslzq.pacsdemo.component

import ItemClickSupport
import android.content.ClipData
import android.content.ClipDescription
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.store.ImageActions
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
        render(store::thumbList) {
            val adapter = view.adapter
            if (adapter != null && adapter is DicomSeriesThumbListAdpater) {
                adapter.thumbList.clear()
                adapter.thumbList.addAll(store.thumbList)
            } else {
                view.adapter = DicomSeriesThumbListAdpater(store.thumbList)
            }
            view.adapter.notifyDataSetChanged()
            if (store.thumbList.isNotEmpty()) {
                ItemClickSupport.addTo(view).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                        if (store.layoutOption == PacsStore.LayoutOption.ONE_ONE && position in (0..(store.thumbList.size - 1))) {
                            store.dispatch(PacsStore.ThumbListItemClicked(position))
                            store.imageCells.first().dispatch(ImageActions.bindModel(store.thumbList[position].modId))
                        }
                    }
                })
                ItemClickSupport.addTo(view).setOnItemLongClickListener(object : ItemClickSupport.OnItemLongClickListener {
                    @Suppress("DEPRECATION")
                    override fun onItemLongClicked(recyclerView: RecyclerView, position: Int, v: View): Boolean {
                        getThumbView(position)?.let {
                            val dragBuilder = View.DragShadowBuilder(it)
                            val clipDataItem = ClipData.Item(tag, position.toString())
                            val clipData = ClipData(tag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), clipDataItem)
                            it.startDrag(clipData, dragBuilder, null, 0)
                        }
                        return true
                    }
                })
            }
        }

        render(store::selected) {
            setSelected(it)
        }
    }

    private fun setSelected(selected: Int) {
        repeat(store.thumbList.size) {
            setSelected(it, false)
        }
        if (selected in (0..(store.thumbList.size - 1))) {
            setSelected(selected, true)
        }
    }

    private fun setSelected(position: Int, selected: Boolean) {
        getThumbView(position)?.isSelected = selected
    }

    private fun getThumbView(position: Int): ImageView? {
        val viewHolder = view.findViewHolderForAdapterPosition(position)
        return viewHolder?.let {
            if (it is DicomSeriesThumbListAdpater.ViewHolder) {
                it.thumbView
            } else {
                null
            }
        }
    }

    companion object {
        val tag = "thumbList"
    }
}