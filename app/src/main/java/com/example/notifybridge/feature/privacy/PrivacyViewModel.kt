package com.example.notifybridge.feature.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifybridge.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacyUiState(
    val prominentDisclosureAccepted: Boolean = false,
)

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<PrivacyUiState> = settingsRepository.observeSettings()
        .map { settings ->
            PrivacyUiState(
                prominentDisclosureAccepted = settings.prominentDisclosureAccepted,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PrivacyUiState(),
        )

    fun acceptDisclosure() {
        viewModelScope.launch {
            settingsRepository.updateSettings { current ->
                current.copy(prominentDisclosureAccepted = true)
            }
        }
    }
}
