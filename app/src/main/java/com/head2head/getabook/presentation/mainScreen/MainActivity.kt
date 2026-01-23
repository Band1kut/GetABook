package com.head2head.getabook.presentation.mainScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.head2head.getabook.presentation.search.SearchActivity
import com.head2head.getabook.presentation.settings.SettingsActivity
import com.head2head.getabook.ui.theme.GetABookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GetABookTheme {
                MainScreen(
                    viewModel = viewModel,
                    onSearch = { query ->
                        val intent = Intent(this, SearchActivity::class.java)
                        intent.putExtra("query", query)
                        startActivity(intent)
                    },
                    onSettingsClick = {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
