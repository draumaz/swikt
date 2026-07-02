package org.terciolab.wiktionaryapp.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView() {
    val scrollState = rememberScrollState()
    var isAboutExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ) 
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Information",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )

            // The "Big ol long button" for About
            AboutButton(
                isExpanded = isAboutExpanded,
                onClick = { isAboutExpanded = !isAboutExpanded }
            )

            AnimatedVisibility(
                visible = isAboutExpanded,
                enter = fadeIn() + expandVertically(animationSpec = spring()),
                exit = fadeOut() + shrinkVertically(animationSpec = spring())
            ) {
                AboutContent()
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun AboutButton(isExpanded: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "About Simple Wiktionary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AboutContent() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val versionText = packageInfo?.let {
        @Suppress("DEPRECATION")
        "v${it.versionName} (${it.versionCode})"
    } ?: "v1.0.0"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = versionText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "An open-source, ad-free Wiktionary client providing a modern interface for exploring the world's languages.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Developers",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ContributorItem(
                    name = "Terciocode",
                    role = "Original Creator",
                    imageUrl = "https://github.com/Terciocode.png",
                    onClick = { uriHandler.openUri("https://github.com/Terciocode") }
                )
                
                ContributorItem(
                    name = "draumaz",
                    role = "Genuinely just forked the repo",
                    imageUrl = "https://github.com/draumaz.png",
                    onClick = { uriHandler.openUri("https://github.com/draumaz") }
                )
                
                ContributorItem(
                    name = "Gemini",
                    role = "Vibe code vanguard",
                    icon = Icons.Default.AutoAwesome,
                    onClick = { uriHandler.openUri("https://developer.android.com/studio/gemini") }
                )
            }
        }
    }
}

@Composable
fun ContributorItem(
    name: String, 
    role: String, 
    icon: ImageVector? = null, 
    imageUrl: String? = null, 
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
