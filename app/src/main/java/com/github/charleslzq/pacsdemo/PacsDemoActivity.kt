package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import com.github.charleslzq.dicom.data.DicomSeries
import kotlinx.android.synthetic.main.pacs_demo_layout.*
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import java.io.File
import java.net.URI


class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val series = emptyList<DicomSeries>().toMutableList()
    private val adapter = DicomSeriesAdpater(series)
    private val patientList = listOf("03117795").toMutableList()
    private lateinit var animationViewManager: AnimationViewManager
    private var patientId = "03117795"
    private var selectedView: View? = null
    private val thumbClickHandler = object : ItemClickSupport.OnItemClickListener {
        override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
            setImage(position)
            selectedView?.isSelected = false
            selectedView?.clearAnimation()
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
            series.clear()
            val newSeries = patient.studies
                    .flatMap { it.series }
                    .sortedBy { it.metaInfo.instanceUID }
                    .toList()
            series.addAll(newSeries)
            Log.i("test", "fetch images ${newSeries.size}")
            this.adapter.notifyDataSetChanged()
            setImage(0)
        }
    }

    private fun setImage(position: Int) {
        val leftImageUri = series[position].images[0].files[DicomSeriesAdpater.DEFAULT]
//        image_left.setImageBitmap(BitmapFactory.decodeFile(File(leftImageUri).absolutePath))
        val imageUrls = series[position].images.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[DicomSeriesAdpater.DEFAULT] }.toList()
        animationViewManager = AnimationViewManager(
                this.resources,
                imageUrls,
                image_left,
                40
        )

        when (position) {
            in 0..(series.size - 2) -> {
                val rightImageUrl = series[position + 1].images[0].files[DicomSeriesAdpater.DEFAULT]
                image_right.setImageBitmap(BitmapFactory.decodeFile(File(rightImageUrl).absolutePath))
            }
            else -> image_right.setImageBitmap(BitmapFactory.decodeFile(File(leftImageUri).absolutePath))
        }
    }


}
