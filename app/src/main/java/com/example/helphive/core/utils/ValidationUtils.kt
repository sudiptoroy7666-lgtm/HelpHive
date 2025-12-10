package com.example.helphive.core.utils


object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidText(text: String, minLength: Int = 1, maxLength: Int = 200): Boolean {
        return text.isNotBlank() && text.trim().length in minLength..maxLength
    }
}