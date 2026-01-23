package com.head2head.getabook.presentation.mainScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.head2head.getabook.presentation.mainScreen.components.HeaderSection
import com.head2head.getabook.presentation.mainScreen.components.SearchBarSection
import com.head2head.getabook.presentation.mainScreen.components.SearchHistorySection

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onSearch: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val searchEngine by viewModel.searchEngine.collectAsState()

    Scaffold(
        topBar = {
            HeaderSection(
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SearchBarSection(
                searchEngine = searchEngine,
                onSearch = onSearch
            )

            Spacer(Modifier.height(12.dp))

            SearchHistorySection(
                modifier = Modifier.weight(1f)
            )
        }
    }
}
