package com.tuapp.tripadvisor.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tuapp.tripadvisor.data.preferences.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfigUiState(
    val pricePerKmInput: String = "",
    val earningsPerHourInput: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ConfigViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { prefs ->
                _uiState.update { current ->
                    current.copy(
                        pricePerKmInput = if (prefs.minPricePerKm > 0) prefs.minPricePerKm.toString() else "",
                        earningsPerHourInput = if (prefs.minEarningsPerHour > 0) prefs.minEarningsPerHour.toString() else ""
                    )
                }
            }
        }
    }

    fun onPricePerKmChanged(value: String) {
        _uiState.update { it.copy(pricePerKmInput = value, errorMessage = null, saveSuccess = false) }
    }

    fun onEarningsPerHourChanged(value: String) {
        _uiState.update { it.copy(earningsPerHourInput = value, errorMessage = null, saveSuccess = false) }
    }

    fun saveAndActivate(onSuccess: () -> Unit) {
        val price = _uiState.value.pricePerKmInput.toDoubleOrNull()
        val earnings = _uiState.value.earningsPerHourInput.toDoubleOrNull()

        if (price == null || price <= 0.0 || earnings == null || earnings <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Ingresa números válidos mayores a cero.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.savePreferences(price, earnings)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            onSuccess()
        }
    }
}

class ConfigViewModelFactory(private val repository: PreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
