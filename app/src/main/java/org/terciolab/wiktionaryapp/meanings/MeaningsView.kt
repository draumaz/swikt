package org.terciolab.wiktionaryapp.meanings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.terciolab.wiktionaryapp.api.Sense
import org.terciolab.wiktionaryapp.api.Sound
import org.terciolab.wiktionaryapp.api.WordMeaning
import org.terciolab.wiktionaryapp.ui.theme.WiktionaryAppTheme
import java.util.Locale

@Composable
fun MeaningsView(word: String, lang: String, viewModel: MeaningsViewModel = viewModel()) {
    val wordMeanings by viewModel.wordMeanings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(word) {
        viewModel.fetchWordMeanings(word, lang)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(wordMeanings) { wordMeaning ->
                    WordMeaningItem(wordMeaning)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun WordMeaningItem(meaning: WordMeaning) {
    Column(verticalArrangement = Arrangement.Top) {

        Text(
            text = "${meaning.lang} | ${meaning.pos}",
            style = MaterialTheme.typography.headlineSmall
        )

        meaning.sounds?.let {
            // ipa without tags
            val ipa : String = meaning.sounds.mapNotNull { it.ipa }.joinToString(", ")

            Text(
                text = "IPA: $ipa",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        meaning.etymology_text?.let {
            Text(
                text = meaning.etymology_text,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }


        Spacer(modifier = Modifier.height(16.dp))


        meaning.senses.forEachIndexed { i, sense ->
            if(sense.glosses != null) {
                Text(
                    text = "${i + 1}. " + sense.glosses.joinToString(". ")
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Left
                )

                Text(
                    text = sense.tags?.joinToString(", ", "[", "]") ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun MeaningsView() {
    WiktionaryAppTheme {

        val wordMeaning = WordMeaning(
            senses = listOf(
                Sense(
                    glosses = listOf("a small number of things or people", "not many"),
                    tags = listOf("quantifier", "determiner")
                ),
                Sense(
                    glosses = listOf("a small amount or quantity"),
                    tags = null
                )
            ),
            pos = "adjective",
            word = "few",
            lang = "en",
            etymology_text = "From Old English 'fēawe', from Proto-Germanic 'fawaz'",
            sounds = listOf(
                Sound(ipa = "/fjuː/", tags = null),
                Sound(ipa = "/fjuː/", tags = null)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = wordMeaning.word,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (false) {
                CircularProgressIndicator()
            } else {
                WordMeaningItem(wordMeaning)
            }
        }
    }
}