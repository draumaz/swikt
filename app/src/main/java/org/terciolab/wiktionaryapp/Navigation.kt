package org.terciolab.wiktionaryapp

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import org.terciolab.wiktionaryapp.meanings.MeaningsView
import org.terciolab.wiktionaryapp.search.SearchView
import org.terciolab.wiktionaryapp.search.SearchViewModel
import org.terciolab.wiktionaryapp.settings.SettingsView
import org.terciolab.wiktionaryapp.settings.SettingsViewModel
import kotlin.math.*

@Stable
class PredictiveBackState {
    var progress by mutableFloatStateOf(0f)
    var isSwipeActive by mutableStateOf(false)
}

class ScallopedPillShape(private val isScalloped: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val width = size.width
        val height = size.height
        val radius = height / 2f

        if (!isScalloped) {
            path.addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    0f, 0f, width, height,
                    androidx.compose.ui.geometry.CornerRadius(radius)
                )
            )
            return Outline.Generic(path)
        }

        val bumpDepth = with(density) { 2.5.dp.toPx() }
        val bumpsCount = 12f
        val numPoints = 120

        fun getPoint(p: Float): Pair<Offset, Offset> {
            val straight = (width - 2 * radius).coerceAtLeast(0f)
            val arc = PI.toFloat() * radius
            val total = 2 * straight + 2 * arc
            val d = p * total
            
            return when {
                d < straight -> {
                    Offset(radius + d, 0f) to Offset(0f, -1f)
                }
                d < straight + arc -> {
                    val angle = 1.5f * PI.toFloat() + (d - straight) / radius
                    val n = Offset(cos(angle), sin(angle))
                    Offset(width - radius, radius) + n * radius to n
                }
                d < 2 * straight + arc -> {
                    Offset(width - radius - (d - (straight + arc)), height) to Offset(0f, 1f)
                }
                else -> {
                    val angle = 0.5f * PI.toFloat() + (d - (2 * straight + arc)) / radius
                    val n = Offset(cos(angle), sin(angle))
                    Offset(radius, radius) + n * radius to n
                }
            }
        }

        for (i in 0..numPoints) {
            val p = i.toFloat() / numPoints
            val (pos, normal) = getPoint(p)
            val bump = sin(p * bumpsCount * 2 * PI.toFloat()) * bumpDepth
            val finalPos = pos + normal * bump
            if (i == 0) path.moveTo(finalPos.x, finalPos.y) else path.lineTo(finalPos.x, finalPos.y)
        }
        
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val predictiveBackState = remember { PredictiveBackState() }

    val showBottomBar = when (currentDestination?.route) {
        Nav.Search.route, Nav.Settings.route, Nav.Details.route -> true
        else -> false
    }

    val bottomBarHeight = 150.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.floatValue + delta
                bottomBarOffsetHeightPx.floatValue = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        NavHost(
            navController = navController,
            startDestination = Nav.Search.route,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    initialOffsetX = { it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    targetOffsetX = { -it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    initialOffsetX = { -it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    targetOffsetX = { it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        ) {
            composable(Nav.Search.route) {
                val searchViewModel: SearchViewModel = viewModel()
                SearchView(navController, searchViewModel)
            }
            composable(Nav.Settings.route) {
                SettingsView(
                    viewModel = settingsViewModel,
                    predictiveBackState = predictiveBackState,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Nav.Details.route,
                arguments = listOf(
                    navArgument("lang") { type = NavType.StringType },
                    navArgument("word") { type = NavType.StringType }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://{lang}.wiktionary.org/wiki/{word}" },
                    navDeepLink { uriPattern = "http://{lang}.wiktionary.org/wiki/{word}" }
                )
            ) { backStackEntry ->
                val word = backStackEntry.arguments?.getString("word")?.replace("_", " ") ?: ""
                val lang = backStackEntry.arguments?.getString("lang") ?: "en"
                MeaningsView(
                    word = word,
                    lang = lang,
                    predictiveBackState = predictiveBackState,
                    onBack = { navController.popBackStack() },
                    onNavigateToWord = { newWord ->
                        navController.navigate("details/$lang/$newWord")
                    }
                )
            }
        }

        if (showBottomBar) {
            val animatedOffset by animateIntAsState(
                targetValue = bottomBarOffsetHeightPx.floatValue.roundToInt(),
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "bottom_bar_offset"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(x = 0, y = -animatedOffset) }
            ) {
                ExpressiveNavigationBar(navController, predictiveBackState)
            }
        }
    }
}

@Composable
fun ExpressiveNavigationBar(
    navController: NavHostController, 
    predictiveBackState: PredictiveBackState
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isOnDefinition = currentDestination?.route == Nav.Details.route
    
    var isScalloped by remember { mutableStateOf(false) }
    val pillShape = remember(isScalloped) { ScallopedPillShape(isScalloped) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .clip(pillShape)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Determine selection alpha based on predictive back
                val isPredictingToHome = predictiveBackState.isSwipeActive && 
                    currentDestination?.route != Nav.Search.route &&
                    navController.previousBackStackEntry?.destination?.route == Nav.Search.route

                val homeSelectionAlpha = if (isPredictingToHome) {
                    predictiveBackState.progress
                } else {
                    val isSelected = currentDestination?.hierarchy?.any { it.route == Nav.Search.route } == true
                    if (isSelected) 1f else 0f
                }

                // Home Item
                ExpressiveNavItem(
                    item = NavigationItem("Home", Nav.Search.route, Icons.Default.Home),
                    isSelected = currentDestination?.hierarchy?.any { it.route == Nav.Search.route } == true,
                    selectionAlphaOverride = homeSelectionAlpha,
                    onLongHold = { isScalloped = !isScalloped },
                    onClick = {
                        if (currentDestination?.route != Nav.Search.route) {
                            navController.popBackStack(Nav.Search.route, inclusive = false)
                        }
                    }
                )

                // The "Melting" Content (Definition + Settings)
                AnimatedContent(
                    targetState = isOnDefinition,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.92f))
                            .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.92f))
                            .using(SizeTransform(clip = false))
                    },
                    label = "pill_melt"
                ) { onDefinition ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onDefinition) {
                            val definitionAlpha = if (predictiveBackState.isSwipeActive) {
                                1f - predictiveBackState.progress
                            } else 1f

                            // Show both when on definition
                            ExpressiveNavItem(
                                item = NavigationItem("Definition", Nav.Details.route, Icons.AutoMirrored.Filled.MenuBook),
                                isSelected = true,
                                selectionAlphaOverride = definitionAlpha,
                                onLongHold = { isScalloped = !isScalloped },
                                onClick = { /* Already here */ }
                            )
                            ExpressiveNavItem(
                                item = NavigationItem("Settings", Nav.Settings.route, Icons.Default.Settings),
                                isSelected = false,
                                onLongHold = { isScalloped = !isScalloped },
                                onClick = {
                                    navController.navigate(Nav.Settings.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        } else {
                            // Just show settings otherwise
                            val isSettingsSelected = currentDestination?.hierarchy?.any { it.route == Nav.Settings.route } == true
                            
                            val isPredictingFromSettings = predictiveBackState.isSwipeActive && 
                                currentDestination?.route == Nav.Settings.route

                            val settingsAlpha = if (isPredictingFromSettings) {
                                1f - predictiveBackState.progress
                            } else {
                                if (isSettingsSelected) 1f else 0f
                            }

                            ExpressiveNavItem(
                                item = NavigationItem("Settings", Nav.Settings.route, Icons.Default.Settings),
                                isSelected = isSettingsSelected,
                                selectionAlphaOverride = settingsAlpha,
                                onLongHold = { isScalloped = !isScalloped },
                                onClick = {
                                    if (!isSettingsSelected) {
                                        navController.navigate(Nav.Settings.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveNavItem(
    item: NavigationItem,
    isSelected: Boolean,
    selectionAlphaOverride: Float? = null,
    onLongHold: () -> Unit = {},
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val job = launch {
                delay(2000)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongHold()
            }
            scale.animateTo(
                0.88f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
            job.join()
        } else {
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "selection_alpha"
    )

    val selectionAlpha = selectionAlphaOverride ?: animatedAlpha
    val containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = selectionAlpha)
    val contentColor = if (selectionAlpha > 0.5f) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            if (selectionAlpha > 0.8f) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

data class NavigationItem(val label: String, val route: String, val icon: ImageVector)

sealed class Nav(val route: String ) {
    object Search           : Nav("search")
    object Settings         : Nav("settings")
    object Details          : Nav("details/{lang}/{word}")
}
