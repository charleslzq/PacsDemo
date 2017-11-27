package com.github.charleslzq.pacsdemo

import ItemClickSupport
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.github.charleslzq.pacsdemo.binder.ImageCellViewBinder
import com.github.charleslzq.pacsdemo.gesture.PresentationMode
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel
import com.github.charleslzq.pacsdemo.vo.PatientSeriesViewModel
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.layout_pacs_demo.*

class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val patientList = listOf("03117795").toMutableList()
    private var patientId = "03117795"
    private val patientSeriesViewModelList: MutableList<PatientSeriesViewModel> = emptyList<PatientSeriesViewModel>().toMutableList()
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

        thumbList.adapter = DicomSeriesThumbListAdpater(patientSeriesViewModelList)
        thumbList.layoutManager = LinearLayoutManager(this)
        thumbList.itemAnimator = SlideInUpAnimator()
        ItemClickSupport.addTo(thumbList).setOnItemClickListener(thumbClickHandler)

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
            patientSeriesViewModelList.clear()
            val newSeries = patient.studies
                    .flatMap { study ->
                        study.series.map { PatientSeriesViewModel(
                                patient.metaInfo,
                                study.metaInfo,
                                it.metaInfo,
                                ImageFramesViewModel(it.images.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[PatientSeriesViewModel.DEFAULT] }),
                                it.images.sortedBy { it.instanceNumber?.toInt() }[0].files[PatientSeriesViewModel.THUMB]!!
                        )}
                    }
                    .sortedBy { it.dicomSeriesMetaInfo.instanceUID }
                    .toList()
            patientSeriesViewModelList.addAll(newSeries)
            Log.i("test", "fetch images ${newSeries.size}")
            thumbList.adapter.notifyDataSetChanged()
            if (patientSeriesViewModelList.isNotEmpty()) {
                changeSeries(0)
            }
        }
    }

    private fun changeSeries(position: Int) {
        val binders = getImageViewBindersFromPanel()
        if (binders.size > 1) {
            binders.filterIndexed { index, _ ->
                index + position < patientSeriesViewModelList.size
            }.forEachIndexed { index, holder ->
                holder.model = patientSeriesViewModelList[index + position]
                holder.model!!.imageFramesViewModel.presentationMode = PresentationMode.SLIDE
            }
            imageSeekBar.visibility = View.INVISIBLE
        } else if (binders.size == 1) {
            val binder = binders[0]
            binder.model = patientSeriesViewModelList[position]
            val model = binder.model!!
            model.imageFramesViewModel.presentationMode = PresentationMode.ANIMATE
            if (model.imageFramesViewModel.presentationMode == PresentationMode.ANIMATE) {
                binder.onModelChange(model.imageFramesViewModel::currentIndex) { _, newIndex ->
                    imageSeekBar.progress = newIndex
                }

                imageSeekBar.max = model.imageFramesViewModel.size
                imageSeekBar.progress = 0
                imageSeekBar.visibility = View.VISIBLE
                imageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            model.imageFramesViewModel.currentIndex = progress
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {

                    }
                })
            } else {
                imageSeekBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun getImageViewBindersFromPanel(): List<ImageCellViewBinder> {
        val displayedChild = viewSelector.getChildAt(viewSelector.displayedChild)
        return when (Option.values()[viewSelector.displayedChild]) {
            Option.ONE_ONE -> {
                listOf(ImageCellViewBinder(displayedChild))
            }
            Option.ONE_TWO -> {
                ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                        .map { ImageCellViewBinder(it) }
            }
            else -> {
                ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                        .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                        .map { ImageCellViewBinder(it) }
            }
        }
    }

    enum class Option {
        ONE_ONE,
        ONE_TWO,
        TWO_TWO,
        THREE_THREE
    }
}
