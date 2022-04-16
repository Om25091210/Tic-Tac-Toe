package `in`.aryomtech.tictactoe.fcm

import `in`.aryomtech.tictactoe.MainActivity
import `in`.aryomtech.tictactoe.R
import `in`.aryomtech.tictactoe.Splash
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

private const val CHANNEL_ID="my_channel"

class FirebaseService :FirebaseMessagingService(){


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.e("newToken", p0)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent=Intent(this,Splash::class.java)
        val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotifionChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent=PendingIntent.getActivity(this,0,intent,FLAG_ONE_SHOT)

            val contentView = RemoteViews(this.packageName, R.layout.notification_layout)
            contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher)
            contentView.setOnClickPendingIntent(R.id.flashButton, pendingIntent)
            contentView.setTextViewText(R.id.message, message.data["title"])
            contentView.setTextViewText(R.id.date, message.data["message"])

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setStyle(NotificationCompat.BigTextStyle().bigText(message.data["message"]))
                .setSmallIcon(R.drawable.ic_logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_img_splash))
                .setAutoCancel(true)
                .setContent(contentView)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(notificationID, notification)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotifionChannel(notificationManager: NotificationManager){
        val channelName="ChannelName"
        val channel=NotificationChannel(CHANNEL_ID,channelName,IMPORTANCE_HIGH).apply {

            description="My channel description"
            enableLights(true)
            lightColor=Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }
}
