package org.terciolab.wiktionaryapp.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.terciolab.wiktionaryapp.Language
import org.terciolab.wiktionaryapp.api.ApiClient
import org.terciolab.wiktionaryapp.api.SearchWord
import org.terciolab.wiktionaryapp.getLanguageByCode
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds


class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val locales = application.resources.configuration.locales

    private val _searchResults = MutableStateFlow<List<SearchWord>>(emptyList())
    val searchResults: StateFlow<List<SearchWord>> = _searchResults

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _selectedLanguage = MutableStateFlow(getLanguageByCode(getCurrentLocale().language))
    val selectedLanguage: StateFlow<Language> = _selectedLanguage

    fun searchWord(query: String) {
        _query.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(300.milliseconds)
                if (_query.value != query) return@launch // Debounce check
                val results = ApiClient.getWiki(selectedLanguage.value.code).suggestWords(query)

                _searchResults.value = results.pages
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                Log.e("SearchViewModel", "Error searching for word '$query': ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearQuery() {
        _query.value = ""
        _searchResults.value = emptyList()
    }

    fun setLanguage(language: Language) {
        _selectedLanguage.update { language }
    }

    fun getCurrentLocale(): Locale {
        return locales[0]
    }
}
