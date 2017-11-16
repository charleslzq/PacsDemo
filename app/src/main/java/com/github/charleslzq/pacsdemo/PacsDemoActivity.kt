package com.github.charleslzq.pacsdemo

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.hello_world_layout.*
import com.github.charleslzq.pacsdemo.service.DicomDataService
import com.github.charleslzq.pacsdemo.service.SimpleServiceConnection
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackgroud


class PacsDemoActivity : AppCompatActivity() {

    private val serviceConnection = SimpleServiceConnection<DicomDataService>(this::dicomDataService::set)
    private var dicomDataService: DicomDataService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hello_world_layout)
        Log.d("PacsDemoActivity", "onCreate execute")
        bindService(Intent(this, DicomDataServiceBackgroud::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        startService.setOnClickListener {
            val patient = dicomDataService?.findPatient("03117795")
            if (patient != null) {
                Log.i("test", "$patient")
            }
        }
        stopService.setOnClickListener {
        }
    }
}
