package com.head2head.getabook.presentation.mainScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "GetABook",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }
        },
        windowInsets = WindowInsets(0.dp) // делает шапку компактнее
    )
}

