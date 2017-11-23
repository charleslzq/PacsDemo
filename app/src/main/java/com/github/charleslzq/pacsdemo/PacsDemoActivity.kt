package com.github.charleslzq.pacsdemo

import ItemClickSupport
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.*
import com.github.charleslzq.dicom.data.DicomSeries
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.layout_pacs_demo.*

class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val patientList = listOf("03117795").toMutableList()
    private var patientId = "03117795"
    private var selectedView: View? = null
    private var currentPosition = 0
    private lateinit var popupMenu: PopupMenu
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
        setContentView(R.layout.layout_pacs_demo)
        Log.d("PacsDemoActivity", "onCreate execute")
        thumbList.adapter = DicomSeriesThumbListAdpater(emptyList<DicomSeries>().toMutableList())
        thumbList.layoutManager = LinearLayoutManager(this)
        thumbList.itemAnimator = SlideInUpAnimator()
        popupMenu = PopupMenu(this, spliteButton)
        popupMenu.menu.add(Menu.NONE, R.id.one_one, Menu.NONE, "1 X 1")
        popupMenu.menu.add(Menu.NONE, R.id.one_two, Menu.NONE, "1 X 2")
        popupMenu.menu.add(Menu.NONE, R.id.two_two, Menu.NONE, "2 X 2")
        popupMenu.menu.add(Menu.NONE, R.id.three_three, Menu.NONE, "3 X 3")
        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected)
        spliteButton.setOnTouchListener { view, _ ->
            view.performClick()
            popupMenu.show()
            true
        }
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
            val adapter = thumbList.adapter as DicomSeriesThumbListAdpater
            adapter.series.clear()
            val newSeries = patient.studies
                    .flatMap { it.series }
                    .sortedBy { it.metaInfo.instanceUID }
                    .toList()
            adapter.series.addAll(newSeries)
            Log.i("test", "fetch images ${newSeries.size}")
            adapter.notifyDataSetChanged()
            if (adapter.series.isNotEmpty()) {
                changeSeries(0)
            }
        }
    }

    private fun changeSeries(position: Int) {
        val imageUrls = (thumbList.adapter as DicomSeriesThumbListAdpater).series[position].images.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[DicomSeriesThumbListAdpater.DEFAULT] }.toList()
        when (Option.values()[viewSelector.displayedChild]) {
            PacsDemoActivity.Option.ONE_ONE -> {
                val animatedImage = getImageViewFromView(imagePanel_1)
                animatedImage.mode = ImageListView.Mode.ANIMATE
                animatedImage.bindUrls(imageUrls, { imageSeekBar.progress = it + 1 }, { it.reset() })
                if (imageUrls.size > 1) {
                    val gestureDetector = GestureDetector(this, ImageAnimationGestureListener(animatedImage))
                    val scaleGestureDetector = ScaleGestureDetector(this, ImageScaleGestureListener(animatedImage))
                    animatedImage.setOnTouchListener(CompositeTouchEventListener(listOf(
                            { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) },
                            { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) }
                    )))
                    imageSeekBar.max = animatedImage.imageFramesState.size
                    imageSeekBar.progress = 1
                    imageSeekBar.visibility = View.VISIBLE
                    imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                animatedImage.changeProgress(progress)
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
                val imageList = getRelativeLayoutFromView(imagePanel_2, R.id.imageColumn_1, R.id.imageColumn_2)
                        .map { getImageViewFromView(it) }
                bindImage(position, imageList)
                imageSeekBar.visibility = View.INVISIBLE
            }
            PacsDemoActivity.Option.TWO_TWO -> {
                val imageList = getTableRowFromTable(imagePanel_3, R.id.imageRow_1, R.id.imageRow_2)
                        .flatMap { getRelativeLayoutFromView(it, R.id.imageColumn_1, R.id.imageColumn_2) }
                        .map { getImageViewFromView(it) }
                bindImage(position, imageList)
                imageSeekBar.visibility = View.INVISIBLE
            }
            PacsDemoActivity.Option.THREE_THREE -> {
                val imageList = getTableRowFromTable(imagePanel_4, R.id.imageRow_1, R.id.imageRow_2, R.id.imageRow_3)
                        .flatMap { getRelativeLayoutFromView(it, R.id.imageColumn_1, R.id.imageColumn_2, R.id.imageColumn_3) }
                        .map { getImageViewFromView(it) }
                bindImage(position, imageList)
                imageSeekBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun getImageViewFromView(view: View): ImageListView {
        return view.findViewById(R.id.imagesContainer)
    }

    private fun getRelativeLayoutFromView(view: View, vararg id: Int): List<RelativeLayout> {
        return id.map { view.findViewById<RelativeLayout>(it) }
    }

    private fun getTableRowFromTable(tableLayout: TableLayout, vararg id: Int): List<TableRow> {
        return id.map { tableLayout.findViewById<TableRow>(it) }
    }

    private fun bindImage(position: Int, viewList: List<ImageListView>) {
        val series = (thumbList.adapter as DicomSeriesThumbListAdpater).series
        viewList.filterIndexed { index, _ -> position + index < series.size }
                .forEachIndexed { index, view ->
                    val imageUrls = series[position + index].images.mapNotNull { it.files[DicomSeriesThumbListAdpater.DEFAULT] }
                    view.mode = ImageListView.Mode.SLIDE
                    view.bindUrls(imageUrls)
                    val gestureDetector = GestureDetector(this, ImageSlideGestureListener(view))
                    val scaleGestureDetector = ScaleGestureDetector(this, ImageScaleGestureListener(view))
                    view.setOnTouchListener(CompositeTouchEventListener(listOf(
                            { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) },
                            { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) }
                    )))
                }
    }

    enum class Option {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}
