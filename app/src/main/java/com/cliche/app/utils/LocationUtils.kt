package com.cliche.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LatLng(val latitude: Double, val longitude: Double)

/**
 * Utilitaires pour la gestion de la localisation.
 */
object LocationUtils {
    /**
     * Return true if the app has either fine or coarse location permission.
     *
     * @param context Context of the application.
     * @return Boolean indicating if location permission is granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Check whether location services are enabled on the device.
     */
    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }


    /**
     * Get the current location of the device.
     *
     * @param context Context of the application.
     * @return LatLng object containing latitude and longitude, or null if location cannot be obtained
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LatLng? {
        if (!hasLocationPermission(context)) return null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { cont ->
            val cancelToken = CancellationTokenSource()

            // Prefer high accuracy when FINE location is granted, else use balanced
            val hasFine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            fusedClient.getCurrentLocation(
                if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancelToken.token
            )
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(LatLng(location.latitude, location.longitude))
                    } else {
                        fusedClient.lastLocation
                            .addOnSuccessListener { last ->
                                cont.resume(last?.let { LatLng(it.latitude, it.longitude) })
                            }
                            .addOnFailureListener {
                                cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    fusedClient.lastLocation
                        .addOnSuccessListener { last ->
                            cont.resume(last?.let { LatLng(it.latitude, it.longitude) })
                        }
                        .addOnFailureListener {
                            cont.resume(null)
                        }
                }

            cont.invokeOnCancellation { cancelToken.cancel() }
        }
    }

    /**
     * Request location permission if not granted, check if location services are enabled,
     * and get the current location. Should be called within a LifecycleCoroutineScope.
     *
     * @param context Context of the application.
     * @param activity Activity to request permissions.
     * @return LatLng object containing latitude and longitude, or null if permission is denied or location services are disabled.
     */
    suspend fun requestLocationOnLifecycle(context: Context, activity: Activity): LatLng? {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1001
            )
            return null
        }

        // Ensure location settings are enabled, prompting the user with a resolvable dialog if needed.
        val enabled = ensureLocationEnabled(activity)
        if (!enabled) {
            Toast.makeText(context, "Please enable location services", Toast.LENGTH_LONG).show()
            return null
        }

        return getCurrentLocation(context)
    }

    /**
     * Check and, if needed, prompt the user to enable Location settings via a resolvable dialog.
     * Returns true when settings are satisfied or the user accepts the prompt; false otherwise.
     *
     * @param activity Activity to use for launching the resolution dialog.
     */
    private suspend fun ensureLocationEnabled(activity: Activity): Boolean {
        val settingsClient = LocationServices.getSettingsClient(activity)
        val request = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).build()
        val settingsRequest = com.google.android.gms.location.LocationSettingsRequest.Builder()
            .addLocationRequest(request)
            .setAlwaysShow(true)
            .build()

        return suspendCancellableCoroutine { cont ->
            settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener {
                    cont.resume(true)
                }
                .addOnFailureListener { ex ->
                    val resolvable = ex as? com.google.android.gms.common.api.ResolvableApiException
                    val componentActivity = activity as? ComponentActivity
                    if (resolvable != null && componentActivity != null) {
                        try {
                            val intentSender = resolvable.resolution.intentSender
                            val key = "loc_enable_" + System.currentTimeMillis()
                            var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
                            launcher = componentActivity.activityResultRegistry.register(
                                key,
                                ActivityResultContracts.StartIntentSenderForResult()
                            ) { result ->
                                launcher?.unregister()
                                cont.resume(result.resultCode == Activity.RESULT_OK)
                            }
                            launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                        } catch (e: Exception) {
                            cont.resume(false)
                        }
                    } else {
                        cont.resume(false)
                    }
                }
        }
    }
}
