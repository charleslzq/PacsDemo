package com.github.charleslzq.pacsdemo.support

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageThumbModel

/**
 * Created by charleslzq on 17-11-20.
 */
class DicomSeriesThumbListAdpater(
        val thumbList: MutableList<ImageThumbModel>
) : RecyclerView.Adapter<DicomSeriesThumbListAdpater.ViewHolder>(), RxScheduleSupport {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.thumbView.setImageBitmap(thumbList[position].thumb)
    }

    override fun getItemCount(): Int {
        return thumbList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.piece_thumb_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(
            view: View,
            val thumbView: ImageView = view.findViewById(R.id.thumbView)
    ) : RecyclerView.ViewHolder(view)
}