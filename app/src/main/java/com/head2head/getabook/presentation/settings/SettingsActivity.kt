package com.head2head.getabook.presentation.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()

            SettingsScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}
