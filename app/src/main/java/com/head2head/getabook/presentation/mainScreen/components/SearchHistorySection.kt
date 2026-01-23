package com.head2head.getabook.presentation.mainScreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchHistorySection(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Text("История поиска появится здесь")
    }
}
