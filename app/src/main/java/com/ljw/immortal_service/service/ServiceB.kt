package com.ljw.immortal_service.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ljw.immortal_service.MainActivity
import com.ljw.immortal_service.PreferenceUnit
import com.ljw.immortal_service.R

class ServiceB : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("ljwLog", "onStartCommand in ServiceB")

        sendNotification()

        if (intent.getStringExtra("type") == "A") {
            startService(Intent(this, ServiceA::class.java)) // MainService() start
        }

        return START_NOT_STICKY
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("ljwLog", "onDestroy in ServiceB, preIsRealStop : " + PreferenceUnit.getInstance().preIsRealStop)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!PreferenceUnit.getInstance().preIsRealStop) {
                callAlarmManger() //
            }
        }
    }

    private fun sendNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val channelId = CHANNEL_ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("B")
            .setAutoCancel(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = CHANNEL_NAME
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = notificationBuilder.build()
        startForeground(9, notification)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun callAlarmManger() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3)

        val intent = Intent(this, RestartAlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = sender
    }

    companion object {
        private const val CHANNEL_ID = "ID_ServiceB"
        private const val CHANNEL_NAME = "NAME_ServiceB"
    }
}

class RestartAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mintent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mintent = Intent(context, ServiceA::class.java)
            mintent.putExtra("type", "B")
            context.startForegroundService(mintent)
        } else {
            mintent = Intent(context, ServiceB::class.java)
            context.startService(mintent)
            Toast.makeText(context, "startService (under Oreo)", Toast.LENGTH_LONG).show()
        }
    }
}
