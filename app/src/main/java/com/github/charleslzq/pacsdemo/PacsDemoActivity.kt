package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import com.github.charleslzq.pacsdemo.binder.PacsMainViewBinder
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel
import com.github.charleslzq.pacsdemo.vo.PacsDemoViewModel
import com.github.charleslzq.pacsdemo.vo.PatientSeriesViewModel
import kotlinx.android.synthetic.main.layout_pacs_demo.*

class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val patientList = listOf("03117795").toMutableList()
    private var patientId = "03117795"
    private lateinit var pacsMainViewBinder: PacsMainViewBinder
    private lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pacs_demo)
        Log.d("PacsDemoActivity", "onCreate execute")

        pacsMainViewBinder = PacsMainViewBinder(pacsPanel)
        thumbList.layoutManager = LinearLayoutManager(this)

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
                pacsMainViewBinder.model?.layoutOption = PacsDemoViewModel.LayoutOption.ONE_ONE
            }
            R.id.one_two -> {
                pacsMainViewBinder.model?.layoutOption = PacsDemoViewModel.LayoutOption.ONE_TWO
            }
            R.id.two_two -> {
                pacsMainViewBinder.model?.layoutOption = PacsDemoViewModel.LayoutOption.TWO_TWO
            }
            R.id.three_three -> {
                pacsMainViewBinder.model?.layoutOption = PacsDemoViewModel.LayoutOption.THREE_THREE
            }
        }
        return true
    }

    private fun refresh() {
        val patient = dicomDataService?.findPatient(patientId)
        if (patient != null) {
            pacsMainViewBinder.model = PacsDemoViewModel(patient.studies.flatMap { study ->
                study.series.sortedBy { it.metaInfo.instanceUID }.map {
                    PatientSeriesViewModel(
                            patient.metaInfo,
                            study.metaInfo,
                            it.metaInfo,
                            ImageFramesViewModel(it.images.sortedBy { it.instanceNumber?.toInt() }.mapNotNull { it.files[PatientSeriesViewModel.DEFAULT] }),
                            it.images.sortedBy { it.instanceNumber?.toInt() }[0].files[PatientSeriesViewModel.THUMB]!!
                    )
                }
            }.toMutableList())
        }
    }
}
