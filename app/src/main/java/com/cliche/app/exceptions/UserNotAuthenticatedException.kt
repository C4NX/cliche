package com.cliche.app.exceptions

class UserNotAuthenticatedException : Exception() {
    override val message: String
        get() = "User is not authenticated."
}