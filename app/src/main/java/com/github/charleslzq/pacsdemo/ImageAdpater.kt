package com.github.charleslzq.pacsdemo

import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import java.io.File

/**
 * Created by charleslzq on 17-11-16.
 */
class ImageAdpater(private val images: MutableList<DicomImageMetaInfo>): RecyclerView.Adapter<ImageAdpater.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thumbUrl = images[position].files[THUMB]
        holder.thumbView.setImageBitmap(BitmapFactory.decodeFile(File(thumbUrl).absolutePath))
    }

    class ViewHolder(
            view: View,
            val thumbView: ImageView = view.findViewById(R.id.thumbView)
    ): RecyclerView.ViewHolder(view)

    companion object {
        val THUMB = "thumb"
        val DEFAULT = "default"
        val RAW = "raw"
    }
}