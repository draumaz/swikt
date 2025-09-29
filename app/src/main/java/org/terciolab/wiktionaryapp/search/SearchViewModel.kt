package org.terciolab.wiktionaryapp.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
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


class SearchViewModel(context: Context) : ViewModel() {

    private val locales = context.resources.configuration.locales

    private val _searchResults = MutableStateFlow<List<SearchWord>>(emptyList())
    val searchResults: StateFlow<List<SearchWord>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedLanguage = MutableStateFlow(getLanguageByCode(getCurrentLocale().language))
    val selectedLanguage: StateFlow<Language> = _selectedLanguage

    fun searchWord(query: String) {
        if(query.length < 1){
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(300)
                val results = ApiClient.getWiki(selectedLanguage.value.code).suggestWords(query)

                _searchResults.value = results.pages
            } catch (e: Exception) {
                _searchResults.value = emptyList() // Handle error
                Log.e("SearchViewModel", "Error searching for word '$query': ${e.message}", e)
                // Don't re-throw the exception, just log it and return empty results
            } finally {
                _isLoading.value = false
            }
        }

    }

    fun setLanguage(language: Language) {
        _selectedLanguage.update { language }
    }

    fun clearList(){
        _searchResults.value = emptyList()
    }

    fun getCurrentLocale() : Locale {
        return locales[0]
    }

}

