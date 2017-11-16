package com.github.charleslzq.pacsdemo

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

/**
 * Created by charleslzq on 17-11-16.
 */
class ImageAdpater(private val images: MutableList<Bitmap>): RecyclerView.Adapter<ImageAdpater.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageBitmap(images[position])
    }

    class ViewHolder(
            view: View,
            val imageView: ImageView = view.findViewById(R.id.image)
    ): RecyclerView.ViewHolder(view)
}