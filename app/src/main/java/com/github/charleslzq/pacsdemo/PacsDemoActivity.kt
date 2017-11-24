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
import com.github.charleslzq.pacsdemo.image.ImageListView
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
                        study.series.map { PatientSeriesViewModel(patient.metaInfo, study.metaInfo, it) }
                    }
                    .sortedBy { it.dicomSeries.metaInfo.instanceUID }
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
        val holders = getImageViewHoldersFromPanel()
        if (holders.size > 1) {
            holders.filterIndexed { index, _ ->
                index + position < patientSeriesViewModelList.size
            }.forEachIndexed { index, holder ->
                holder.bindData(patientSeriesViewModelList[index + position])
            }
            imageSeekBar.visibility = View.INVISIBLE
        } else if (holders.size == 1){
            val holder = holders[0]
            holder.bindData(patientSeriesViewModelList[position], ImageListView.Mode.ANIMATE)
            if (holder.image.mode == ImageListView.Mode.ANIMATE) {
                val animatedImage = holder.image
                val originalIndexListener = animatedImage.imageFramesState.indexChangeListener
                animatedImage.imageFramesState.indexChangeListener = {
                    originalIndexListener.invoke(it)
                    imageSeekBar.progress = it + 1
                }
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
            } else {
                imageSeekBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun getImageViewHoldersFromPanel(): List<ImageCellViewHolder> {
        val displayedChild = viewSelector.getChildAt(viewSelector.displayedChild)
        return when(Option.values()[viewSelector.displayedChild]) {
            Option.ONE_ONE -> {
                listOf(ImageCellViewHolder(displayedChild))
            }
            Option.ONE_TWO -> {
                ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                        .map { ImageCellViewHolder(it) }
            }
            else -> {
                ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                        .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                        .map { ImageCellViewHolder(it) }
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
