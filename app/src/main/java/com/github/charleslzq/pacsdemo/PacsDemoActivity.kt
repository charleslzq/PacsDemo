package com.github.charleslzq.pacsdemo

import ItemClickSupport
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.github.charleslzq.dicom.data.DicomSeries
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.pacs_demo_layout.*


class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val series = emptyList<DicomSeries>().toMutableList()
    private val adapter = DicomSeriesAdpater(series)
    private val patientList = listOf("03117795").toMutableList()
    private var patientId = "03117795"
    private var selectedView: View? = null
    private val thumbClickHandler = object : ItemClickSupport.OnItemClickListener {
        override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
            changeSeries(position)
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
            series.clear()
            val newSeries = patient.studies
                    .flatMap { it.series }
                    .sortedBy { it.metaInfo.instanceUID }
                    .toList()
            series.addAll(newSeries)
            Log.i("test", "fetch images ${newSeries.size}")
            this.adapter.notifyDataSetChanged()
            changeSeries(0)
        }
    }

    private fun changeSeries(position: Int) {
        val imageUrls = series[position].images.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[DicomSeriesAdpater.DEFAULT] }.toList()
        val animationViewManager = AnimationViewManager(animated_image, imageUrls, this::setSeekBarProgress)
        if (imageUrls.size > 1) {
            animated_image.setOnClickListener(AnimationImageClickListener(animationViewManager))
            imageSeekBar.max = animationViewManager.numOfFrames
            imageSeekBar.progress = 1
            imageSeekBar.visibility = View.VISIBLE
            imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        animationViewManager.changePosition(progress - 1)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
        } else if (imageUrls.size == 1){
            imageSeekBar.visibility = View.INVISIBLE
        }
    }

    private fun setSeekBarProgress(index: Int) {
        imageSeekBar.progress = index + 1
    }


}
