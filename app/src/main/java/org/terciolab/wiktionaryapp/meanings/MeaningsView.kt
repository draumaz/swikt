package org.terciolab.wiktionaryapp.meanings

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.terciolab.wiktionaryapp.api.WordMeaning
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MeaningsView(
    word: String,
    lang: String,
    predictiveBackState: org.terciolab.wiktionaryapp.PredictiveBackState,
    onBack: () -> Unit,
    onNavigateToWord: (String) -> Unit,
    viewModel: MeaningsViewModel = viewModel(),
) {
    val wordMeanings by viewModel.wordMeanings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    PredictiveBackHandler(enabled = true) { progress ->
        predictiveBackState.isSwipeActive = true
        try {
            progress.collect { event ->
                predictiveBackState.progress = event.progress
            }
            predictiveBackState.isSwipeActive = false
            predictiveBackState.progress = 0f
            onBack()
        } catch (ignored: Exception) {
            predictiveBackState.isSwipeActive = false
            predictiveBackState.progress = 0f
        }
    }

    LaunchedEffect(word) {
        viewModel.fetchWordMeanings(word, lang)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .graphicsLayer {
                val p = predictiveBackState.progress
                val s = 1f - (p * 0.08f)
                scaleX = s
                scaleY = s
                translationX = p * 400f
                alpha = 1f - (p * 0.2f)
                clip = true
                shape = RoundedCornerShape((p * 28.dp.toPx()).coerceAtLeast(0f))
            },
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
    var isExpanded by remember { mutableStateOf(value = true) }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isExpanded = !isExpanded 
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = meaning.pos.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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
                
                IconButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isExpanded = !isExpanded 
                }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(150)),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Column {
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

                    meaning.forms?.let { forms ->
                        GrammarSection(forms)
                        Spacer(modifier = Modifier.height(16.dp))
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
                                                    text = (i + 1).toString(),
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
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GrammarSection(forms: List<org.terciolab.wiktionaryapp.api.WordForm>) {
    val cases = listOf("nominative", "genitive", "dative", "accusative", "instrumental", "prepositional")
    val numbers = listOf("singular", "plural")

    // Filter forms that have both a case and a number tag
    val declensionMap = mutableMapOf<Pair<String, String>, String>()
    forms.forEach { form ->
        val tags = form.tags ?: emptyList()
        val c = cases.find { tags.contains(it) }
        val n = numbers.find { tags.contains(it) }
        if ((c != null) && (n != null)) {
            declensionMap[Pair(c, n)] = form.form
        }
    }

    if (declensionMap.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Declension",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                // Header Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) // Empty corner
                    numbers.forEach { number ->
                        Text(
                            text = number.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                cases.forEach { case ->
                    if (numbers.any { n -> declensionMap.containsKey(Pair(case, n)) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Case Name
                            Text(
                                text = case.take(3).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )

                            numbers.forEach { number ->
                                val formText = declensionMap[Pair(case, number)] ?: "—"
                                Text(
                                    text = formText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (formText == "—") MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }
                        if (case != cases.last()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    } else {
        // Fallback for other types of inflections
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            forms.take(8).forEach { form ->
                SuggestionChip(
                    onClick = { },
                    label = { 
                        Text(
                            text = "${form.form} (${form.tags?.joinToString(", ")})",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    }
                )
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
        val wikiRegex = Regex("""\[\[([^|\]#]+)(?:#[^|\]]+)?(?:\|([^]]+))?]]""")
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
