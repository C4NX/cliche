package com.cliche.app.services.realtime

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import android.app.PendingIntent
import com.cliche.app.R
import com.cliche.app.models.events.LikeEvent
import com.cliche.app.modules.supabaseClient
import com.cliche.app.services.api.AppApi
import com.cliche.app.services.auth.AuthManager
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Foreground service that subscribes to per-user likes channel and shows notifications
 * on incoming like events, even when the app isn't open.
 */
class LikesForegroundService : Service() {

    companion object {
        const val TAG = "LikesForegroundService"
        private const val SERVICE_CHANNEL_ID = "likes_listener_channel"
        private const val ALERT_CHANNEL_ID = "likes_notifications_channel"

        fun start(context: Context) {
            val intent = Intent(context, LikesForegroundService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }

    private var scope: CoroutineScope? = null
    private var job: Job? = null
    private var channel: RealtimeChannel? = null

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(Dispatchers.IO)
        createServiceNotificationChannel()
        createAlertNotificationChannel()
    }

    @OptIn(SupabaseExperimental::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LikesForegroundService starting...")
        startForeground(1, buildServiceNotification())

        // Launch listener
        if (job?.isActive == true) return START_STICKY
        job = scope?.launch {
            try {
                AppApi.authManager.waitToBeReady()

                val currentUser = AppApi.authManager.getUserOrNull()
                if (currentUser == null) {
                    Log.w(TAG, "Cannot start LikesRealtimeService: no authenticated user")
                    return@launch
                }

                val chan = supabaseClient.realtime.channel("likes:${currentUser.id}") {
                    isPrivate = true
                }
                channel = chan

                chan.subscribe()
                coroutineScope {
                    chan.broadcastFlow<LikeEvent>(LikeEvent.EVENT_NAME).collect { payload ->
                        Log.d(TAG, "Broadcast like received for post: ${payload.post_id}")
                        val title = "Notification from Cliche"
                        val content = "Like on your post !"
                        showAlertNotification(title, content, payload.post_id)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in LikesForegroundService: ${e.message}", e)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            scope?.cancel()
            job?.cancel()
            job = null
            runBlocking {
                channel?.unsubscribe()
            }
            channel = null;
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createServiceNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "Cliché",
            NotificationManager.IMPORTANCE_MIN
        )
        nm.createNotificationChannel(channel)
    }

    private fun createAlertNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ALERT_CHANNEL_ID,
            getString(R.string.title_notifications),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)
    }

    private fun buildServiceNotification(): Notification {
        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_24dp)
            .setContentTitle("Cliché")
            .setContentText("Notifications service is running !")
            .setOngoing(true)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun showAlertNotification(title: String, content: String, postId: Long?) {
        val intent = Intent(this, com.cliche.app.LoginActivity::class.java).apply {
            if (postId != null)
                putExtra("postId", postId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val requestCode = postId?.hashCode() ?: 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_24dp)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
