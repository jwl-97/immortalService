package com.ljw.immortal_service

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ljw.immortal_service.service.ServiceA
import com.ljw.immortal_service.service.ServiceB

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_start_service -> {
                startService(Intent(this, ServiceA::class.java))
                startService(Intent(this, ServiceB::class.java))
            }

            R.id.btn_stop_service_a -> stopService(Intent(applicationContext, ServiceA::class.java))
            R.id.btn_stop_service_b -> stopService(Intent(applicationContext, ServiceB::class.java))

            R.id.btn_stop_service_all -> {
                PreferenceUnit.getInstance().preIsRealStop = true
                stopService(Intent(applicationContext, ServiceA::class.java))
                stopService(Intent(applicationContext, ServiceB::class.java))
            }
        }
    }
}
