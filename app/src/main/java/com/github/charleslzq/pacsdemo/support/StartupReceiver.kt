package com.github.charleslzq.pacsdemo.support

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.charleslzq.pacsdemo.service.background.DicomDataServiceBackground

class StartupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, DicomDataServiceBackground::class.java))
    }
}
