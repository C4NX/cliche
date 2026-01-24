package com.cliche.app.utils

import com.cliche.app.services.auth.AuthManager

/**
 * Retourne l'ID de l'utilisateur actuellement authentifié.
 * Lance une exception si l'utilisateur n'est pas authentifié.
 */
fun requireUserId(): String {
    val user = AuthManager.getUserOrNull()
    require(user != null) { "User must be authenticated" }
    return user.id
}