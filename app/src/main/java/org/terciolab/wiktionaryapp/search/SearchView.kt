package org.terciolab.wiktionaryapp.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.terciolab.wiktionaryapp.Language
import org.terciolab.wiktionaryapp.R
import org.terciolab.wiktionaryapp.api.SearchWord

@Composable
fun SearchView(navController: NavController, viewModel: SearchViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()

    Column(modifier = Modifier.padding(10.dp))
    {
        SearchBar(query, selectedLang,
            onLanguageSelected = { lang ->
                viewModel.setLanguage(lang)
                viewModel.clearList()
            },
            onQueryChange = {
                query = it
                viewModel.searchWord(it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            WordList(searchResults, navController, selectedLang )
        }
    }
}

@Composable
fun SearchBar(query : String, selectedLang: Language, onLanguageSelected: (Language) -> Unit, onQueryChange: (String) -> Unit){
    Text(
        text = stringResource(R.string.app_name),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )

    Row {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
            singleLine = true,
            trailingIcon = {
                LanguageSelectionButton(
                    selectedLanguage = selectedLang,
                    onLanguageSelected = { lang ->
                        onLanguageSelected(lang)
                    }
                )
            }
        )

    }

}


@Composable
fun WordList(words: List<SearchWord>, navController: NavController, lang: Language) {
    LazyColumn {
        items(words) { word ->
            WordItem(word, {
                navController.navigate("details/${lang.code}/${word.title}")
            })
        }
    }
}

@Composable
fun WordItem(word: SearchWord, onClickWord: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
            .clickable {
                onClickWord()
            }
    ) {
        Text(
            text = word.title,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}


@Composable
fun LanguageSelectionButton(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    var showLanguageMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showLanguageMenu = true }) {
        Text(selectedLanguage.code.uppercase())
    }

    DropdownMenu(
        expanded = showLanguageMenu,
        onDismissRequest = { showLanguageMenu = false }
    ) {
        Language.entries.forEach { language ->
            DropdownMenuItem(
                text = { Text("${language.displayName} (${language.code.uppercase()})") },
                onClick = {
                    onLanguageSelected(language)
                    showLanguageMenu = false
                }
            )
        }
    }
}
