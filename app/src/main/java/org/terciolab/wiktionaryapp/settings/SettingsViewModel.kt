package org.terciolab.wiktionaryapp.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _isAmoled = MutableStateFlow(prefs.getBoolean("amoled", false))
    val isAmoled: StateFlow<Boolean> = _isAmoled

    fun setAmoled(value: Boolean) {
        _isAmoled.value = value
        prefs.edit().putBoolean("amoled", value).apply()
    }
}
