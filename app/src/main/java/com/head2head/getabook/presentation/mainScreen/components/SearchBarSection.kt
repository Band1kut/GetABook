package com.head2head.getabook.presentation.mainScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import com.head2head.getabook.data.settings.SearchEngine

@Composable
fun SearchBarSection(
    searchEngine: SearchEngine,
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Поиск в ${searchEngine.displayName}") },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            maxLines = Int.MAX_VALUE,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            shape = MaterialTheme.shapes.medium
        )

        Button(
            onClick = { onSearch(query) },
            modifier = Modifier
                .height(56.dp)
                .width(56.dp),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = "Найти")
        }
    }
}

private val SearchEngine.displayName: String
    get() = when (this) {
        SearchEngine.YANDEX -> "Яндекс"
        SearchEngine.GOOGLE -> "Google"
    }
