package com.cliche.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cliche.app.services.realtime.LikesForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Starts the likes foreground service on device boot if a user session exists.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootCompletedReceiver", "Boot completed, attempting to start LikesForegroundService")
            CoroutineScope(Dispatchers.IO).launch {
                LikesForegroundService.start(context)
            }
        }
    }
}
