package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.pacs_demo_layout.*
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud
import java.io.File


class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null
    private val bitmaps = emptyList<Bitmap>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pacs_demo_layout)
        Log.d("PacsDemoActivity", "onCreate execute")
        val adapter = ImageAdpater(bitmaps)
        imageList.adapter = adapter
        imageList.layoutManager = LinearLayoutManager(this)
        bindService(Intent(this, DicomDataServiceBackgroud::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        startService.setOnClickListener {
            val patient = dicomDataService?.findPatient("03117795")
            if (patient != null) {
                bitmaps.clear()
                val newUris = patient.studies.flatMap { it.series }.flatMap { it.images }.map { it.files }.flatMap { it.values }
                        .take(10).mapNotNull {
                    BitmapFactory.decodeFile(File(it).absolutePath)
                }.toList()
                bitmaps.addAll(newUris)
                Log.i("test", "fetch images ${newUris.size}")
                adapter.notifyDataSetChanged()
            }
        }
        stopService.setOnClickListener {
        }
    }
}
