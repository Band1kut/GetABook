package com.head2head.getabook.presentation.mainScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.head2head.getabook.data.settings.SearchEngine
import com.head2head.getabook.domain.repository.SettingsRepository
import com.head2head.getabook.domain.repository.SitesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val sitesRepository: SitesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _sitesCount = MutableStateFlow(0)
    val sitesCount: StateFlow<Int> = _sitesCount

    private val _domainsPreview = MutableStateFlow("")
    val domainsPreview: StateFlow<String> = _domainsPreview

    // 🔥 Добавили поток поисковика
    val searchEngine: StateFlow<SearchEngine> =
        settingsRepository.searchEngineFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SearchEngine.GOOGLE
        )

    init {
        Log.d("MainScreenVM", "MainScreenViewModel init started")

        viewModelScope.launch {
            val sites = sitesRepository.getAllSites()
            Log.d("MainScreenVM", "Loaded sites: $sites")

            _sitesCount.value = sites.size
            _domainsPreview.value = sites.take(3).joinToString(", ") { it.domain }

            Log.d("MainScreenVM", "sitesCount=${sites.size}, preview=${_domainsPreview.value}")
        }
    }
}
