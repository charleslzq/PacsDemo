package com.github.charleslzq.pacsdemo.component

import ItemClickSupport
import android.content.ClipData
import android.content.ClipDescription
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.charleslzq.pacsdemo.component.store.ImageDisplayActions
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
                ItemClickSupport.addTo(view)
                    .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                        override fun onItemClicked(
                            recyclerView: RecyclerView,
                            position: Int,
                            v: View
                        ) {
                            if (store.layoutOption == PacsStore.LayoutOption.ONE_ONE && position in 0 until store.thumbList.size) {
                                store.dispatch(PacsStore.ThumbListItemClicked(position))
                                store.imageCells.first()
                                    .dispatch(ImageDisplayActions.bindModel(getModId(position)))
                            }
                        }
                    })
                ItemClickSupport.addTo(view)
                    .setOnItemLongClickListener(object : ItemClickSupport.OnItemLongClickListener {
                        @Suppress("DEPRECATION")
                        override fun onItemLongClicked(
                            recyclerView: RecyclerView,
                            position: Int,
                            v: View
                        ): Boolean {
                            if (store.layoutOption != PacsStore.LayoutOption.ONE_ONE) {
                                getThumbView(position)?.let {
                                    val dragBuilder = View.DragShadowBuilder(it)
                                    val clipData = ClipData(
                                        tag,
                                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                                        ClipData.Item(tag)
                                    )
                                    it.startDrag(clipData, dragBuilder, getModId(position), 0)
                                }
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

    private fun getModId(position: Int) = store.thumbList[position].modId

    private fun setSelected(selected: Int) {
        repeat(store.thumbList.size) {
            setSelected(it, false)
        }
        if (selected in 0 until store.thumbList.size) {
            setSelected(selected, true)
        }
    }

    private fun setSelected(position: Int, selected: Boolean) {
        getThumbView(position)?.isSelected = selected
    }

    private fun getThumbView(position: Int) =
        (view.findViewHolderForAdapterPosition(position) as? DicomSeriesThumbListAdpater.ViewHolder)?.thumbView

    companion object {
        val tag = "thumbList"
    }
}