package com.ljw.immortal_service.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ljw.immortal_service.MainActivity
import com.ljw.immortal_service.PreferenceUnit
import com.ljw.immortal_service.R

class ServiceA : Service() {

    override fun onCreate() {
        super.onCreate()

        PreferenceUnit.init(applicationContext)
        sendNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ljwLog", "onStartCommand in ServiceA")

        if (intent?.getStringExtra("type") == "B") {
            startService(Intent(this, ServiceB::class.java))
        }

        if (PreferenceUnit.getInstance().preIsRealStop) {
            PreferenceUnit.getInstance().preIsRealStop = false
            Log.d("ljwLog", "change preIsRealStop true -> false")
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("ljwLog", "onDestroy in ServiceA, preIsRealStop : " + PreferenceUnit.getInstance().preIsRealStop)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            /**
             * 가이드)
             *  예를 들면, 엡 실행시 '출근하기' 버튼을 누르면 위치 서비스가 계속 돌아야 하는 앱이 있다.
             *  '퇴근하기' 혹은 '로그아웃' 시 서비스를 종료해야하고, 그 외에에는 서비스가 계속 돌아야한다.
             *  서비스 진짜 종료(RealStop)와 가짜종료(앱이 죽거나 모종의 이유로 서비스종료)를 구분하기 위해
             *  preIsRealStop 라는 변수를 preference에 저장해 가지고 있는다.
             */
            if (!PreferenceUnit.getInstance().preIsRealStop) {
                callAlarmManger() //
            }
        }
    }

    private fun sendNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val channelId = CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("A")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = CHANNEL_NAME
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = notificationBuilder.build()
        startForeground(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun callAlarmManger() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3)

        val intent = Intent(this, AlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = sender
    }

    companion object {
        private const val CHANNEL_ID = "ID_ServiceA"
        private const val CHANNEL_NAME = "NAME_ServiceA"
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        /**
         * 오레오 이상부터는 백그라운드 서비스 실행을 막기 때문에, restart하는 서비스가 필요
         */
        val mintent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mintent = Intent(context, ServiceB::class.java)
            mintent.putExtra("type", "A")
            context.startForegroundService(mintent)
        } else {
            mintent = Intent(context, ServiceA::class.java)
            context.startService(mintent)
            Toast.makeText(context, "startService (under Oreo)", Toast.LENGTH_LONG).show()
        }
    }
}
