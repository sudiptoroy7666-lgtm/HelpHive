package com.example.helphive.ui.profile

import com.example.helphive.data.model.User
import android.graphics.Bitmap

data class ProfileUiState(
    val user: User? = null,
    val profileImageBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)