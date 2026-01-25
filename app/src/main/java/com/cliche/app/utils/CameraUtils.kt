package com.cliche.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.cliche.app.R
import java.io.File

/**
 * Utility functions for camera and image handling.
 */
object CameraUtils {
    /**
     * Shows a dialog to select image source: gallery or camera.
     * @param fragment The fragment from which the dialog is shown.
     * @param onPickGallery Callback to execute when gallery is selected.
     * @param onCapture Callback to execute when camera is selected.
     */
    fun showImageSourceDialog(
        fragment: Fragment,
        onPickGallery: () -> Unit,
        onCapture: () -> Unit
    ) {
        val ctx = fragment.requireContext()
        val options = arrayOf(
            ctx.getString(R.string.add_post_select_from_gallery),
            ctx.getString(R.string.add_post_select_capture)
        )
        val builder = android.app.AlertDialog.Builder(ctx)
        builder.setTitle(ctx.getString(R.string.add_post_select_title))
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> onPickGallery()
                1 -> onCapture()
            }
        }
        builder.setNegativeButton(ctx.getString(R.string.cancel), null)
        builder.show()
    }

    /**
     * Opens the camera after checking for camera permission.
     * @param fragment The fragment from which the camera is opened.
     * @param requestPermissionLauncher The launcher to request camera permission.
     * @param onGranted Callback to execute if permission is granted.
     */
    fun openCameraWithPermissionCheck(
        fragment: Fragment,
        requestPermissionLauncher: ActivityResultLauncher<String>,
        onGranted: () -> Unit
    ) {
        val ctx = fragment.requireContext()
        when {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            fragment.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(ctx, ctx.getString(R.string.camera_permission_rationale), Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Prepares a temporary image file and its URI for camera capture.
     * @param context The context used to create the file.
     * @return A pair containing the URI and the File object.
     */
    fun prepareTempImageUri(context: Context): Pair<Uri, File> {
        val photoFile = createTempImageFile(context)
        val authority = context.packageName + ".fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, photoFile)
        return Pair(uri, photoFile)
    }
}
