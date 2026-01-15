package com.head2head.getabook

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.head2head.getabook.data.SitesManager
import com.head2head.getabook.search.SearchActivity
import com.head2head.getabook.ui.theme.GetABookTheme
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "MainActivity создан")

        setContent {
            GetABookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity возобновлён")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val TAG = "MainScreen"
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Получаем информацию о сайтах
    val sitesCount = remember { SitesManager.getSiteCount() }
    val domainsPreview = remember {
        SitesManager.getAllDomains().take(3).joinToString(", ")
    }

    Log.d(TAG, "Composable MainScreen отрисован, сайтов: $sitesCount")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GetABook") },
                actions = {
                    Text(
                        text = "$sitesCount сайтов",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Поисковая строка
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        Log.v(TAG, "Ввод в поиске: '${it.take(20)}...' (${it.length} символов)")
                        searchQuery = it
                    },
                    label = { Text("Название книги") },
                    placeholder = { Text("Введите название книги...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                // Информация о поддерживаемых сайтах
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Ищет по $sitesCount сайтам:",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = domainsPreview + if (sitesCount > 3) "..." else "",
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Кнопка поиска
                Button(
                    onClick = {
                        Log.i(TAG, "Нажата кнопка поиска, запрос: '${searchQuery.take(50)}...'")

                        if (searchQuery.isNotBlank()) {
                            // Передаём СЫРОЙ запрос
                            val intent = Intent(context, SearchActivity::class.java).apply {
                                putExtra("RAW_QUERY", searchQuery)
                            }

                            Log.d(TAG, "Запуск SearchActivity с запросом длиной ${searchQuery.length}")
                            context.startActivity(intent)

                        } else {
                            Log.w(TAG, "Попытка поиска с пустым запросом")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = searchQuery.isNotBlank()
                ) {
                    Text("Найти книгу")
                }

                // Заглушка для будущей истории/библиотеки
                Text(
                    text = "Здесь будет история поиска и библиотека книг",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}