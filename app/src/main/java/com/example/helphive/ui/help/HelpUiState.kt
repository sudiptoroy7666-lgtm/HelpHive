// Add this to the help package or in a shared models file
package com.example.helphive.ui.help

import com.example.helphive.data.model.HelpRequest

data class HelpUiState(
    val helpRequests: List<HelpRequest> = emptyList(),
    val isLoading: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)