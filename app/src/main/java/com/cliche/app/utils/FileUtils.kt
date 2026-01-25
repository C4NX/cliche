package com.cliche.app.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Copie le contenu du [uri] dans un fichier temporaire situé dans les fichiers externes de l'app
 * (ou internes en fallback) et retourne le File créé.
 *
 * Lance une exception si l'URI ne peut pas être ouvert ou si la copie échoue.
 */
@Throws(Exception::class)
fun copyUriToGalleryTempFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Unable to open input stream for URI: $uri")

    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
        .format(java.util.Date())
    val storageDir = context.getExternalFilesDir(null) ?: context.filesDir
    val tempFile = File.createTempFile("GALLERY_$timeStamp", ".jpg", storageDir)

    inputStream.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return tempFile
}

/**
 * Crée un fichier image temporaire dans les fichiers externes de l'application.
 *
 * Le fichier est nommé avec un timestamp pour garantir l'unicité.
 *
 * @param context Le contexte de l'application.
 * @return Le fichier image temporaire créé.
 * @throws Exception Si la création du fichier échoue.
 */
@Throws(Exception::class)
fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}