package org.terciolab.wiktionaryapp.meanings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.terciolab.wiktionaryapp.api.ApiClient
import org.terciolab.wiktionaryapp.api.WordMeaning
import org.terciolab.wiktionaryapp.getLanguageByCode


class MeaningsViewModel : ViewModel() {

    private val _wordMeanings = MutableStateFlow<List<WordMeaning>>(emptyList())
    val wordMeanings: StateFlow<List<WordMeaning>> = _wordMeanings

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchWordMeanings(word: String, langCode: String) {
        val langPrefix = getLanguageByCode(
            langCode,
        ).prefix

        if (word.isEmpty()) {
            _wordMeanings.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val firstOne = word.take(1)
                val firstTwo = if (word.length >= 2) word.take(2) else word.take(1) + "_"
                
                val meanings = ApiClient.kaikki.getWordMeanings(
                    firstOne,
                    firstTwo,
                    word,
                    langPrefix
                )
                _wordMeanings.value = meanings
            } catch (e: Exception) {
                _wordMeanings.value = emptyList()
                Log.e("MeaningsViewModel", "Error fetching meanings for word '$word': ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
