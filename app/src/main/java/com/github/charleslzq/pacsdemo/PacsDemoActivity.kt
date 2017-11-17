package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.github.charleslzq.dicom.data.DicomImageMetaInfo
import kotlinx.android.synthetic.main.pacs_demo_layout.*
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import java.io.File


class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val images = emptyList<DicomImageMetaInfo>().toMutableList()
    private val adapter = ImageAdpater(images)
    private var patientId = "03117795"
    private var selectedView: View? = null
    private val thumbClickHandler = object: ItemClickSupport.OnItemClickListener {
        override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
            setImage(position)
            selectedView?.isSelected = false
            selectedView = v
            selectedView?.isSelected = true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pacs_demo_layout)
        Log.d("PacsDemoActivity", "onCreate execute")
        thumbList.adapter = this.adapter
        thumbList.layoutManager = LinearLayoutManager(this)
        thumbList.itemAnimator = SlideInUpAnimator()
        ItemClickSupport.addTo(thumbList).setOnItemClickListener(thumbClickHandler)
        bindService(Intent(this, DicomDataServiceBackgroud::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        refresh()
        refreshButton.setOnClickListener { refresh() }
    }

    private fun refresh() {
        val patient = dicomDataService?.findPatient(patientId)
        if (patient != null) {
            images.clear()
            val newImages = patient.studies
                    .flatMap { it.series }
                    .flatMap { it.images }
                    .toList()
            images.addAll(newImages)
            Log.i("test", "fetch images ${newImages.size}")
            this.adapter.notifyDataSetChanged()
            setImage(0)
        }
    }

    private fun setImage(position: Int) {
        val leftImageUri = images[position].files[ImageAdpater.DEFAULT]
        image_left.setImageBitmap(BitmapFactory.decodeFile(File(leftImageUri).absolutePath))
        when (position) {
            in 0..(images.size-2) -> {
                val rightImageUrl = images[position+1].files[ImageAdpater.DEFAULT]
                image_right.setImageBitmap(BitmapFactory.decodeFile(File(rightImageUrl).absolutePath))
            }
            else -> image_right.setImageBitmap(BitmapFactory.decodeFile(File(leftImageUri).absolutePath))
        }
    }
}
