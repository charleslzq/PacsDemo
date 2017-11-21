package com.github.charleslzq.pacsdemo

import ItemClickSupport
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.github.charleslzq.dicom.data.DicomSeries
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.pacs_demo_layout.*
import java.io.File


class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val series = emptyList<DicomSeries>().toMutableList()
    private val adapter = DicomSeriesAdpater(series)
    private val patientList = listOf("03117795").toMutableList()
    private var patientId = "03117795"
    private var selectedView: View? = null
    private var currentPosition = 0
    private val thumbClickHandler = object : ItemClickSupport.OnItemClickListener {
        override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
            changeSeries(position)
            selectedView?.isSelected = false
            selectedView = v
            selectedView?.isSelected = true
            currentPosition = position
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pacs_demo_layout)
        Log.d("PacsDemoActivity", "onCreate execute")
        thumbList.adapter = this.adapter
        thumbList.layoutManager = LinearLayoutManager(this)
        thumbList.itemAnimator = SlideInUpAnimator()
        viewSelector.displayedChild = Option.ONE_ONE.ordinal
        ItemClickSupport.addTo(thumbList).setOnItemClickListener(thumbClickHandler)
        bindService(Intent(this, DicomDataServiceBackgroud::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        refresh()
        refreshButton.setOnClickListener { refresh() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_layout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.one_one -> {
                viewSelector.displayedChild = Option.ONE_ONE.ordinal
                changeSeries(currentPosition)
            }
            R.id.one_two -> {
                viewSelector.displayedChild = Option.ONE_TWO.ordinal
                changeSeries(currentPosition)
            }
            R.id.two_two -> {
                viewSelector.displayedChild = Option.TWO_TWO.ordinal
                changeSeries(currentPosition)
            }
            R.id.three_three -> {
                viewSelector.displayedChild = Option.THREE_THREE.ordinal
                changeSeries(currentPosition)
            }
        }
        return true
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
        when (Option.values()[viewSelector.displayedChild]) {
            PacsDemoActivity.Option.ONE_ONE -> {
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
                } else if (imageUrls.size == 1) {
                    imageSeekBar.visibility = View.INVISIBLE
                }
            }
            PacsDemoActivity.Option.ONE_TWO -> {
                val imageList = listOf(image_1_2_1, image_1_2_2)
                val seekBarList = listOf(imageSeekBar_1_2_1, imageSeekBar_1_2_2)
                bindImage(position, imageList, seekBarList)
            }
            PacsDemoActivity.Option.TWO_TWO -> {
                val imageList = listOf(image_2_2_1, image_2_2_2, image_2_2_3, image_2_2_4)
                val seekBarList = listOf(imageSeekBar_2_2_1, imageSeekBar_2_2_2, imageSeekBar_2_2_3, imageSeekBar_2_2_4)
                bindImage(position, imageList, seekBarList)
            }
            PacsDemoActivity.Option.THREE_THREE -> {
                val imageList = listOf(image_3_3_1, image_3_3_2, image_3_3_3, image_3_3_4, image_3_3_5, image_3_3_6, image_3_3_7, image_3_3_8, image_3_3_9)
                val seekBarList = listOf(imageSeekBar_3_3_1, imageSeekBar_3_3_2, imageSeekBar_3_3_3, imageSeekBar_3_3_4, imageSeekBar_3_3_5, imageSeekBar_3_3_6, imageSeekBar_3_3_7, imageSeekBar_3_3_8, imageSeekBar_3_3_9)
                bindImage(position, imageList, seekBarList)
            }
        }
    }

    private fun bindImage(position: Int,viewList: List<ImageView>, seekbarList: List<SeekBar>) {
        for (i in 0 until viewList.size) {
            val it = position + i
            if (it >= series.size) {
                return
            }
            val imageUrls = series[it].images.mapNotNull { it.files[DicomSeriesAdpater.DEFAULT] }
            val bitmap = BitmapFactory.decodeFile(File(imageUrls[0]).absolutePath)
            viewList[i].setImageBitmap(bitmap)
            if (i <= seekbarList.size - 1 && imageUrls.size > 1) {
                seekbarList[i].max = imageUrls.size
                seekbarList[i].progress = 1
                seekbarList[i].visibility = View.VISIBLE
                seekbarList[i].setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val index = (progress + imageUrls.size -1) % imageUrls.size
                            val imageUrl = imageUrls[index]
                            val bitmap = BitmapFactory.decodeFile(File(imageUrl).absolutePath)
                            viewList[i].setImageBitmap(bitmap)
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {

                    }

                })
            }
        }
    }

    private fun setSeekBarProgress(index: Int) {
        imageSeekBar.progress = index + 1
    }

    enum class Option {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}
