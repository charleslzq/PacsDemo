package com.github.charleslzq.pacsdemo

import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.charleslzq.dicom.data.DicomSeries
import java.io.File

/**
 * Created by charleslzq on 17-11-20.
 */
class DicomSeriesAdpater(
        private val series: List<DicomSeries>
) : RecyclerView.Adapter<DicomSeriesAdpater.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thumbUrl = series[position].images[0].files[DicomSeriesAdpater.THUMB]
        holder.thumbView.setImageBitmap(BitmapFactory.decodeFile(File(thumbUrl).absolutePath))
    }

    override fun getItemCount(): Int {
        return series.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return DicomSeriesAdpater.ViewHolder(view)
    }

    class ViewHolder(
            view: View,
            val thumbView: ImageView = view.findViewById(R.id.thumbView)
    ) : RecyclerView.ViewHolder(view)

    companion object {
        val THUMB = "thumb"
        val DEFAULT = "default"
        val RAW = "raw"
    }
}