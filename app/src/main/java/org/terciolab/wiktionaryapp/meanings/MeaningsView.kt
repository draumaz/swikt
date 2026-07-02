package org.terciolab.wiktionaryapp.meanings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.terciolab.wiktionaryapp.api.WordMeaning
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeaningsView(
    word: String,
    lang: String,
    onBack: () -> Unit,
    onNavigateToWord: (String) -> Unit,
    viewModel: MeaningsViewModel = viewModel()
) {
    val wordMeanings by viewModel.wordMeanings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(word) {
        viewModel.fetchWordMeanings(word, lang)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = word,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (wordMeanings.isEmpty()) {
                Text(
                    text = "No definitions found for \"$word\"",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(wordMeanings) { wordMeaning ->
                        WordMeaningItem(wordMeaning, onNavigateToWord)
                    }
                }
            }
        }
    }
}

@Composable
fun WordMeaningItem(meaning: WordMeaning, onNavigateToWord: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meaning.pos.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = meaning.lang,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            meaning.sounds?.let { sounds ->
                val ipa: String = sounds.asSequence().mapNotNull { it.ipa }.joinToString(", ")
                if (ipa.isNotEmpty()) {
                    Text(
                        text = "IPA: $ipa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            meaning.etymology_text?.let { etymology ->
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Etymology & Origins",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            // Decorative full-height vertical line
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(3.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))

                            val linkColor = MaterialTheme.colorScheme.primary
                            val annotatedEtymology = remember(etymology) {
                                parseEtymology(etymology, linkColor)
                            }

                            ClickableText(
                                text = annotatedEtymology,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 24.sp,
                                    letterSpacing = 0.2.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                onClick = { offset ->
                                    annotatedEtymology.getStringAnnotations(tag = "WORD", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            onNavigateToWord(annotation.item)
                                        }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            meaning.senses.forEachIndexed { i, sense ->
                if (sense.glosses != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                // Index badge
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${i + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = sense.glosses.joinToString(". ")
                                        .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            sense.tags?.let { tags ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    tags.forEach { tag ->
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseEtymology(text: String, linkColor: Color): AnnotatedString {
    // Basic cleanup of MediaWiki formatting
    val processedText = text
        .replace("'''", "")
        .replace("''", "")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")

    return buildAnnotatedString {
        val wikiRegex = Regex("""\[\[([^|\]#]+)(?:#[^|\]]+)?(?:\|([^\]]+))?]]""")
        val htmlRegex = Regex("""<a\s+href="[^"]*/wiki/([^"]+)"[^>]*>([^<]*)</a>""")
        
        val markersList = listOf(
            "from", "derived from", "cognate with", "related to", "specifically", 
            "probably", "possibly", "cognates", "cognate", "inherited from", 
            "borrowed from", "adapted from", "calque of", "doublet of", 
            "learned borrowing from", "semi-learned borrowing from",
            "orthographic borrowing from", "unadapted borrowing from",
            "clipping of", "abbreviation of", "shortening of", "surface analysis"
        )
        val markersRegexStr = markersList.joinToString("|") { "(?i)\\b$it\\b" }
        val markersRegex = Regex(markersRegexStr)
        
        // Pattern: [Language or Marker] [Term(s)].
        // Language: Sequence of capitalized words.
        // Terms: Any Unicode letter (\p{L}), mark (\p{M}), or number/subscript (\p{N}).
        val languageNamePattern = """[A-Z]\p{L}+(?:\s+[A-Z]\p{L}+)*"""
        val sourcePattern = """(?:$languageNamePattern|$markersRegexStr)"""
        val termPattern = """[*'’\p{L}\p{M}\p{N}-]+"""
        
        // Use a negative lookahead (?!$sourcePattern) to ensure Group 2 (Term) doesn't 
        // accidentally match a word that should be part of the next Source.
        val langWordRegex = Regex("""($sourcePattern)\s+((?!$sourcePattern)$termPattern(?:,\s+(?!$sourcePattern)$termPattern)*)(?=\s*[(\[.,;]|\s|$)""")
        
        var currentIndex = 0
        
        // Combine and sort all matches
        val allMatches = (wikiRegex.findAll(processedText) + 
                          htmlRegex.findAll(processedText) + 
                          markersRegex.findAll(processedText) +
                          langWordRegex.findAll(processedText))
            .sortedBy { it.range.first }
        
        for (match in allMatches) {
            if (match.range.first < currentIndex) continue
            
            append(processedText.substring(currentIndex, match.range.first))
            
            val value = match.value
            when {
                value.startsWith("[[") || value.startsWith("<a") -> {
                    val (target, display) = if (value.startsWith("[[")) {
                        val t = match.groupValues[1]
                        val d = match.groupValues[2].takeIf { it.isNotEmpty() } ?: t
                        t to d
                    } else {
                        val t = match.groupValues[1].replace("_", " ")
                        val d = match.groupValues[2]
                        t to d
                    }
                    appendLink(target, display, linkColor)
                }
                match.groupValues.size >= 3 && match.groupValues[1].isNotEmpty() && match.groupValues[2].isNotEmpty() -> {
                    // This comes from langWordRegex
                    val langPart = match.groupValues[1]
                    val wordsPart = match.groupValues[2]
                    
                    if (markersRegex.matches(langPart)) {
                        appendMarker(langPart)
                    } else {
                        append(langPart)
                    }
                    append(" ")

                    val termList = wordsPart.split(Regex(""",\s+"""))
                    termList.forEachIndexed { i, term ->
                        val cleanTerm = term.trim().removePrefix("*")
                        appendLink(cleanTerm, term, linkColor)
                        if (i < termList.size - 1) append(", ")
                    }
                }
                markersRegex.matches(value) -> {
                    appendMarker(value)
                }
                else -> {
                    append(value)
                }
            }
            
            currentIndex = match.range.last + 1
        }
        
        if (currentIndex < processedText.length) {
            append(processedText.substring(currentIndex))
        }
    }
}

private fun AnnotatedString.Builder.appendLink(target: String, display: String, color: Color) {
    // Strip anchors for navigation
    val cleanTarget = target.split("#").first()
    pushStringAnnotation(tag = "WORD", annotation = cleanTarget)
    withStyle(style = SpanStyle(
        color = color,
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.Bold
    )) {
        append(display)
    }
    pop()
}

private fun AnnotatedString.Builder.appendMarker(text: String) {
    withStyle(style = SpanStyle(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic
    )) {
        append(text)
    }
}
