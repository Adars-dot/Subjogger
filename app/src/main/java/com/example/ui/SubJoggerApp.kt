package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.SubscriptionItem
import com.example.ui.components.AddSubscriptionSheet
import com.example.ui.components.EditSubscriptionSheet
import com.example.ui.components.PrivacyPromiseDialog
import com.example.ui.components.SubJoggerTopBar
import com.example.ui.navigation.CalendarExportRoute
import com.example.ui.navigation.ConfirmExtractionRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.JoggerRoute
import com.example.ui.navigation.PasteScrubRoute
import com.example.ui.navigation.ReviewQueueRoute
import com.example.ui.navigation.ScreenshotPrepRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.navigation.TotalsRoute
import com.example.ui.screens.CalendarExportScreen
import com.example.ui.screens.ConfirmExtractionScreen
import com.example.ui.screens.HomeListScreen
import com.example.ui.screens.JoggerScreen
import com.example.ui.screens.PasteScrubScreen
import com.example.ui.screens.ReviewQueueScreen
import com.example.ui.screens.ScreenshotPrepScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TotalsScreen
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.viewmodel.SubJoggerViewModel
import kotlinx.coroutines.launch

private data class TopTabItem(
    val route: Any,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun SubJoggerApp(viewModel: SubJoggerViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pasteState by viewModel.pasteScrubState.collectAsStateWithLifecycle()
    val screenshotState by viewModel.screenshotState.collectAsStateWithLifecycle()
    val confirmState by viewModel.confirmState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<SubscriptionItem?>(null) }
    var showCreateManualSheet by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val topTabs = listOf(
        TopTabItem(HomeRoute, R.string.nav_my_list, Icons.Filled.Checklist, Icons.Outlined.Checklist, "nav_my_list"),
        TopTabItem(JoggerRoute, R.string.nav_jogger, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_jogger"),
        TopTabItem(TotalsRoute, R.string.nav_totals, Icons.Filled.PieChart, Icons.Outlined.PieChart, "nav_totals"),
        TopTabItem(SettingsRoute, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    val isTopLevel = currentDest?.let { dest ->
        dest.hasRoute<HomeRoute>() ||
            dest.hasRoute<JoggerRoute>() ||
            dest.hasRoute<TotalsRoute>() ||
            dest.hasRoute<SettingsRoute>()
    } ?: true

    val topBarTitle = when {
        currentDest?.hasRoute<HomeRoute>() == true -> "My List"
        currentDest?.hasRoute<JoggerRoute>() == true -> "Memory Jogger"
        currentDest?.hasRoute<TotalsRoute>() == true -> "Totals"
        currentDest?.hasRoute<SettingsRoute>() == true -> "Settings"
        currentDest?.hasRoute<ReviewQueueRoute>() == true -> "Review Queue"
        currentDest?.hasRoute<CalendarExportRoute>() == true -> "Calendar Export"
        currentDest?.hasRoute<PasteScrubRoute>() == true -> "Paste & Scrub"
        currentDest?.hasRoute<ScreenshotPrepRoute>() == true -> "Screenshot OCR"
        currentDest?.hasRoute<ConfirmExtractionRoute>() == true -> "Confirm Readout"
        else -> "SubJogger"
    }

    fun navigateTopTab(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val ledgerColors = LocalLedgerColors.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && isTopLevel) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    topTabs.forEach { tab ->
                        val selected = when (tab.route) {
                            is HomeRoute -> currentDest?.hasRoute<HomeRoute>() == true
                            is JoggerRoute -> currentDest?.hasRoute<JoggerRoute>() == true
                            is TotalsRoute -> currentDest?.hasRoute<TotalsRoute>() == true
                            is SettingsRoute -> currentDest?.hasRoute<SettingsRoute>() == true
                            else -> false
                        }
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    SubJoggerTopBar(
                        title = topBarTitle,
                        showBack = !isTopLevel,
                        onBackClick = { navController.popBackStack() },
                        showSearchAction = currentDest?.hasRoute<HomeRoute>() == true,
                        onSearchClick = { viewModel.toggleSearch() },
                        onShieldClick = { showPrivacyDialog = true }
                    )
                },
                bottomBar = {
                    if (!useRail && isTopLevel) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            topTabs.forEach { tab ->
                                val selected = when (tab.route) {
                                    is HomeRoute -> currentDest?.hasRoute<HomeRoute>() == true
                                    is JoggerRoute -> currentDest?.hasRoute<JoggerRoute>() == true
                                    is TotalsRoute -> currentDest?.hasRoute<TotalsRoute>() == true
                                    is SettingsRoute -> currentDest?.hasRoute<SettingsRoute>() == true
                                    else -> false
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(tab.labelRes),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = ledgerColors.textSecondary,
                                        unselectedTextColor = ledgerColors.textSecondary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (currentDest?.hasRoute<HomeRoute>() == true || currentDest?.hasRoute<JoggerRoute>() == true) {
                        ExtendedFloatingActionButton(
                            onClick = { showAddSheet = true },
                            icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                            text = { Text(stringResource(R.string.fab_add_subscription)) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(99.dp),
                            modifier = Modifier.testTag("fab_add_subscription")
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable<HomeRoute> {
                        HomeListScreen(
                            state = uiState,
                            onFilterSelected = viewModel::setFilter,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onToggleSearch = viewModel::toggleSearch,
                            onDismissBanner = viewModel::dismissBanner,
                            onOpenCalendarExport = { navController.navigate(CalendarExportRoute) },
                            onOpenReviewQueue = { navController.navigate(ReviewQueueRoute) },
                            onOpenJogger = { navigateTopTab(JoggerRoute) },
                            onItemClick = { item -> editingItem = item },
                            onQuickAddAmount = { item -> editingItem = item },
                            onOpenAddSheet = { showAddSheet = true }
                        )
                    }

                    composable<JoggerRoute> {
                        JoggerScreen(
                            state = uiState,
                            onAddTemplate = viewModel::addJoggerTemplate,
                            onOpenManualAdd = { showCreateManualSheet = true }
                        )
                    }

                    composable<TotalsRoute> {
                        TotalsScreen(
                            state = uiState,
                            onOpenCalendarExport = { navController.navigate(CalendarExportRoute) }
                        )
                    }

                    composable<SettingsRoute> {
                        SettingsScreen(
                            state = uiState,
                            onCurrencyChange = viewModel::setCurrencyCode,
                            onAlertLeadDaysChange = viewModel::setAlertLeadDays,
                            onNotificationsToggle = viewModel::setNotificationsEnabled,
                            onOnDeviceParserToggle = viewModel::setOnDeviceParserEnabled,
                            onTestNotification = viewModel::triggerTestNotification,
                            onOpenCalendarExport = { navController.navigate(CalendarExportRoute) },
                            onExportJsonBackup = viewModel::exportJsonBackupString,
                            onImportJsonBackup = viewModel::importJsonBackup,
                            onClearAllData = viewModel::clearAllData,
                            onResetSampleData = viewModel::resetSampleData
                        )
                    }

                    composable<ReviewQueueRoute> {
                        ReviewQueueScreen(
                            items = uiState.reviewQueueItems,
                            onConfirmItem = viewModel::confirmReviewItem,
                            onEditItem = { item -> editingItem = item },
                            onDeleteItem = { item -> viewModel.deleteSubscription(item.id, item.name) }
                        )
                    }

                    composable<CalendarExportRoute> {
                        CalendarExportScreen(
                            items = uiState.allItems,
                            defaultLeadDays = uiState.preferences.alertLeadDays,
                            onGenerateIcs = viewModel::buildIcsContent,
                            onSavedFeedback = { message ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        )
                    }

                    composable<PasteScrubRoute> {
                        PasteScrubScreen(
                            state = pasteState,
                            onTextChange = viewModel::updatePasteText,
                            onFillSample = viewModel::fillSampleReceipt,
                            onExtractClick = {
                                if (viewModel.extractCandidatesFromScrubbedText()) {
                                    navController.navigate(ConfirmExtractionRoute)
                                }
                            }
                        )
                    }

                    composable<ScreenshotPrepRoute> {
                        ScreenshotPrepScreen(
                            state = screenshotState,
                            onScreenshotPicked = viewModel::onScreenshotSelected,
                            onCropChange = viewModel::updateCropBounds,
                            onRunOcrClick = {
                                viewModel.runOnDeviceOcrExtraction {
                                    navController.navigate(ConfirmExtractionRoute)
                                }
                            }
                        )
                    }

                    composable<ConfirmExtractionRoute> {
                        ConfirmExtractionScreen(
                            state = confirmState,
                            onToggleSelect = viewModel::toggleCandidateSelection,
                            onUpdateCandidate = viewModel::updateCandidate,
                            onSaveSelected = { stageLowConfidence ->
                                viewModel.saveSelectedCandidates(stageLowConfidence)
                                navController.popBackStack(HomeRoute, inclusive = false)
                            },
                            onDiscardAll = {
                                viewModel.discardCandidates()
                                navController.popBackStack(HomeRoute, inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddSubscriptionSheet(
            onDismiss = { showAddSheet = false },
            onPasteTextClick = { navController.navigate(PasteScrubRoute) },
            onUploadScreenshotClick = { navController.navigate(ScreenshotPrepRoute) },
            onManualEntryClick = { showCreateManualSheet = true }
        )
    }

    if (showCreateManualSheet) {
        EditSubscriptionSheet(
            initialItem = null,
            defaultCurrencyCode = uiState.preferences.currencyCode,
            onDismiss = { showCreateManualSheet = false },
            onSave = viewModel::saveSubscription
        )
    }

    editingItem?.let { item ->
        EditSubscriptionSheet(
            initialItem = item,
            defaultCurrencyCode = uiState.preferences.currencyCode,
            onDismiss = { editingItem = null },
            onSave = viewModel::saveSubscription,
            onDelete = viewModel::deleteSubscription
        )
    }

    if (showPrivacyDialog) {
        PrivacyPromiseDialog(onDismiss = { showPrivacyDialog = false })
    }
}
