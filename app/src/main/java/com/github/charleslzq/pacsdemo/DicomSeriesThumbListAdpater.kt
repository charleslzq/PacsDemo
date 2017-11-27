package com.github.charleslzq.pacsdemo

import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.vo.PatientSeriesViewModel
import java.io.File

/**
 * Created by charleslzq on 17-11-20.
 */
class DicomSeriesThumbListAdpater(
        val seriesViewModels: MutableList<PatientSeriesViewModel>
) : RecyclerView.Adapter<DicomSeriesThumbListAdpater.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.thumbView.setImageBitmap(BitmapFactory.decodeFile(File(seriesViewModels[position].thumbUrl).absolutePath))
    }

    override fun getItemCount(): Int {
        return seriesViewModels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.piece_thumb_item, parent, false)
        return DicomSeriesThumbListAdpater.ViewHolder(view)
    }

    class ViewHolder(
            view: View,
            val thumbView: ImageView = view.findViewById(R.id.thumbView)
    ) : RecyclerView.ViewHolder(view)
}