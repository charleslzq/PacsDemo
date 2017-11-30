package com.github.charleslzq.pacsdemo

import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesModel
import java.io.File

/**
 * Created by charleslzq on 17-11-20.
 */
class DicomSeriesThumbListAdpater(
        val seriesModels: MutableList<PatientSeriesModel>
) : RecyclerView.Adapter<DicomSeriesThumbListAdpater.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.thumbView.setImageBitmap(BitmapFactory.decodeFile(File(seriesModels[position].imageFramesModel.thumbUrl).absolutePath))
    }

    override fun getItemCount(): Int {
        return seriesModels.size
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