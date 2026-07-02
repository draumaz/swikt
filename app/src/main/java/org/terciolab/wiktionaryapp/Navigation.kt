package org.terciolab.wiktionaryapp

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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


@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = when (currentDestination?.route) {
        Nav.Search.route, Nav.Settings.route, Nav.Details.route -> true
        else -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Nav.Search.route,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { -it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { -it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { it / 10 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        ) {
            composable(Nav.Search.route) {
                val searchViewModel: SearchViewModel = viewModel()
                SearchView(navController, searchViewModel)
            }
            composable(Nav.Settings.route) {
                SettingsView(settingsViewModel)
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
                    onBack = { navController.popBackStack() },
                    onNavigateToWord = { newWord ->
                        navController.navigate("details/$lang/$newWord")
                    }
                )
            }
        }

        if (showBottomBar) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                ExpressiveNavigationBar(navController)
            }
        }
    }
}

@Composable
fun ExpressiveNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isOnDefinition = currentDestination?.route == Nav.Details.route

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
                .clip(RoundedCornerShape(40.dp))
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Item
                val isHomeSelected = currentDestination?.hierarchy?.any { it.route == Nav.Search.route } == true
                ExpressiveNavItem(
                    item = NavigationItem("Home", Nav.Search.route, Icons.Default.Home),
                    isSelected = isHomeSelected,
                    onClick = {
                        if (!isHomeSelected) {
                            navController.popBackStack(Nav.Search.route, inclusive = false)
                        }
                    }
                )

                // The "Melting" Content (Definition + Settings)
                AnimatedContent(
                    targetState = isOnDefinition,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.92f))
                            .togetherWith(fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.92f))
                            .using(SizeTransform(clip = false))
                    },
                    label = "pill_melt"
                ) { onDefinition ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onDefinition) {
                            // Show both when on definition
                            ExpressiveNavItem(
                                item = NavigationItem("Definition", Nav.Details.route, Icons.AutoMirrored.Filled.MenuBook),
                                isSelected = true,
                                onClick = { /* Already here */ }
                            )
                            ExpressiveNavItem(
                                item = NavigationItem("Settings", Nav.Settings.route, Icons.Default.Settings),
                                isSelected = false,
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
                            ExpressiveNavItem(
                                item = NavigationItem("Settings", Nav.Settings.route, Icons.Default.Settings),
                                isSelected = isSettingsSelected,
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
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(300)
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
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

            if (isSelected) {
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
