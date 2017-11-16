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
        val intent = Intent(this, DicomDataServiceBackgroud::class.java)
        startService.setOnClickListener {
            startService(intent)
        }
        stopService.setOnClickListener {
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.i("test", dicomDataService?.getStore()?.getStoreData()?.patients?.size.toString())
        }
    }
}
