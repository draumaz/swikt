package org.terciolab.wiktionaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import org.terciolab.wiktionaryapp.ui.theme.WiktionaryAppTheme
import org.terciolab.wiktionaryapp.search.SearchView
import org.terciolab.wiktionaryapp.settings.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val amoled by settingsViewModel.isAmoled.collectAsState()

            WiktionaryAppTheme(amoled = amoled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation(settingsViewModel)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppPreview() {
    WiktionaryAppTheme {
        val navController = rememberNavController()
        SearchView(navController)
    }
}