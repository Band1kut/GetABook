package com.head2head.getabook.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.head2head.getabook.data.settings.SearchEngine
import com.head2head.getabook.domain.repository.SettingsRepository
import com.head2head.getabook.domain.repository.SitesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val searchEngine: SearchEngine = SearchEngine.YANDEX,
    val downloadFolder: String? = null,
    val enabledSites: Set<String> = emptySet(),
    val allSites: List<String> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sitesRepository: SitesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.searchEngineFlow,
                settingsRepository.downloadFolderFlow,
                settingsRepository.enabledSitesFlow
            ) { engine, folder, enabledSites ->

                val allSites = sitesRepository.getAllSites().map { it.domain }

                SettingsUiState(
                    searchEngine = engine,
                    downloadFolder = folder,
                    enabledSites = enabledSites,
                    allSites = allSites
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setSearchEngine(engine: SearchEngine) {
        viewModelScope.launch {
            settingsRepository.setSearchEngine(engine)
        }
    }

    fun setDownloadFolder(path: String?) {
        viewModelScope.launch {
            settingsRepository.setDownloadFolder(path)
        }
    }

    fun setEnabledSites(domains: Set<String>) {
        viewModelScope.launch {
            settingsRepository.setEnabledSites(domains)
        }
    }

    fun selectAllSites() {
        viewModelScope.launch {
            val all = sitesRepository.getAllSites().map { it.domain }.toSet()
            settingsRepository.setEnabledSites(all)
        }
    }

    fun deselectAllSites() {
        viewModelScope.launch {
            settingsRepository.setEnabledSites(emptySet())
        }
    }
}
