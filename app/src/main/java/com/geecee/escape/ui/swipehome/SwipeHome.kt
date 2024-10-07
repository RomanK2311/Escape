package com.geecee.escape

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.SwipeableState
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.geecee.escape.ui.theme.JostTypography
import kotlin.math.roundToInt


@OptIn(
    ExperimentalWearMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SwipeHome(
    context: Context,
    packageManager: PackageManager,
    hiddenAppsManager: HiddenAppsManager,
    favoriteAppsManager: FavoriteAppsManager,
    challengesManager: ChallengesManager,
    onOpenSettings: () -> Unit
) {
    // Swipeable stuff
    val width = 1000.dp
    val squareSize = 1000.dp
    val swipeableState = rememberSwipeableState(1)
    val sizePx = with(LocalDensity.current) { squareSize.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1) // Maps anchor points (in px) to states

    //BottomSheet
    val showBottomSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val currentSelectedApp = remember { mutableStateOf("") }
    val currentPackageName = remember { mutableStateOf("") }
    val isCurrentAppFavorite = remember { mutableStateOf(false) }
    val isCurrentAppChallenge = remember { mutableStateOf(false) }
    val isCurrentAppHidden = remember { mutableStateOf(false) }

    //Other
    val haptics = LocalHapticFeedback.current
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )

    //Challenge stuff
    val showOpenChallenge = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(width)
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
            .background(Color.LightGray)
            .combinedClickable(onClick = {}, onLongClickLabel = {}.toString(), onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenSettings()

                val editor = sharedPreferencesSettings.edit()
                editor.putString("FirstTimeAppDrawHelp", "False")
                editor.apply()
            })
    ) {
        SwipeHomeAppsList(
            context = context,
            packageManager = packageManager,
            swipeableState = swipeableState,
            currentAppName = currentSelectedApp,
            currentPackageName = currentPackageName,
            isCurrentAppFavorite = isCurrentAppFavorite,
            isCurrentAppHidden = isCurrentAppHidden,
            isCurrentAppChallenged = isCurrentAppChallenge,
            showBottomSheet = showBottomSheet,
            favoriteAppsManager = favoriteAppsManager,
            hiddenAppsManager = hiddenAppsManager,
            challengesManager = challengesManager,
            showOpenChallenge = showOpenChallenge
        )

    }

    //Bottom Sheet
    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false; },
            sheetState = sheetState
        ) {
            Column(Modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
                Row {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "App Options",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(0.dp, 0.dp, 10.dp, 0.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        currentSelectedApp.value,
                        Modifier,
                        MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        style = JostTypography.titleMedium
                    )
                }
                HorizontalDivider(Modifier.padding(0.dp, 15.dp))
                Column(Modifier.padding(47.dp, 0.dp, 0.dp, 0.dp)) {
                    Text(
                        stringResource(id = R.string.uninstall),
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                // Uninstall logic here
                                val intent = Intent(
                                    Intent.ACTION_DELETE,
                                    Uri.parse("package:${currentPackageName.value}")
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(id = R.string.hide),
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                hiddenAppsManager.addHiddenApp(currentPackageName.value)
                                showBottomSheet.value = false
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (isCurrentAppFavorite.value) stringResource(id = R.string.rem_from_fav) else stringResource(
                            id = R.string.add_to_fav
                        ),
                        modifier = Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                if (isCurrentAppFavorite.value) {
                                    favoriteAppsManager.removeFavoriteApp(currentPackageName.value)
                                } else {
                                    favoriteAppsManager.addFavoriteApp(currentPackageName.value)
                                }
                                // Update the state after the operation
                                isCurrentAppFavorite.value =
                                    favoriteAppsManager.isAppFavorite(currentPackageName.value)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!isCurrentAppChallenge.value) {
                        Text(
                            stringResource(id = R.string.add_open_challenge),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    challengesManager.addChallengeApp(currentPackageName.value)
                                    showBottomSheet.value = false
                                }),
                            MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        stringResource(id = R.string.app_info),
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.parse("package:${currentPackageName.value}")
                                    }
                                context.startActivity(intent)
                                showBottomSheet.value = false
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showOpenChallenge.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        OpenChallenge({
            AppUtils.openApp(packageManager,context,currentPackageName.value,challengesManager,true,null)
            showOpenChallenge.value = false
        }, {
            showOpenChallenge.value = false
        })
    }
}

@Composable
fun SwipeHomeHome() {

}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SwipeHomeAppsList(
    context: Context,
    packageManager: PackageManager,
    swipeableState: SwipeableState<Int>,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showBottomSheet: MutableState<Boolean>,
    hiddenAppsManager: HiddenAppsManager,
    favoriteAppsManager: FavoriteAppsManager,
    challengesManager: ChallengesManager,
    showOpenChallenge: MutableState<Boolean>,
) {
    val installedApps = AppUtils.getAllInstalledApps(packageManager = packageManager)
    val sortedInstalledApps =
        installedApps.sortedBy { getAppNameFromPackageName(context, it.activityInfo.packageName) }
    val scrollState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp, 0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(140.dp))
                Text(
                    text = stringResource(id = R.string.all_apps),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            //TODO: Search bar

            items(sortedInstalledApps) { app ->
                if (app.activityInfo.packageName != "com.geecee.escape" && !hiddenAppsManager.isAppHidden(app.activityInfo.packageName))
                    SwipeAppsListItem(
                        app,
                        context = context,
                        packageManager = packageManager,
                        currentAppName = currentAppName,
                        currentPackageName = currentPackageName,
                        isCurrentAppHidden = isCurrentAppHidden,
                        isCurrentAppChallenged = isCurrentAppChallenged,
                        isCurrentAppFavorite = isCurrentAppFavorite,
                        showBottomSheet = showBottomSheet,
                        challengesManager = challengesManager,
                        hiddenAppsManager = hiddenAppsManager,
                        favoriteAppsManager = favoriteAppsManager,
                        showOpenChallenge = showOpenChallenge
                    )
            }


            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeAppsListItem(
    app: ResolveInfo,
    packageManager: PackageManager,
    context: Context,
    showBottomSheet: MutableState<Boolean>,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showOpenChallenge: MutableState<Boolean>,
    challengesManager: ChallengesManager,
    favoriteAppsManager: FavoriteAppsManager,
    hiddenAppsManager: HiddenAppsManager
) {
    Text(
        AppUtils.getAppNameFromPackageName(context, app.activityInfo.packageName),
        modifier = Modifier
            .padding(vertical = 15.dp)
            .combinedClickable(
                onClick = {
                    val packageName = app.activityInfo.packageName
                    currentPackageName.value = packageName

                    AppUtils.openApp(
                        packageManager = packageManager,
                        context = context,
                        packageName,
                        challengesManager,
                        false,
                        showOpenChallenge
                    )
                },
                onLongClick = {
                    showBottomSheet.value = true
                    currentAppName.value = AppUtils.getAppNameFromPackageName(context, app.activityInfo.packageName)
                    currentPackageName.value = app.activityInfo.packageName
                    isCurrentAppChallenged.value = challengesManager.doesAppHaveChallenge(AppUtils.getAppNameFromPackageName(context, app.activityInfo.packageName))
                    isCurrentAppHidden.value = hiddenAppsManager.isAppHidden(AppUtils.getAppNameFromPackageName(context, app.activityInfo.packageName))
                    isCurrentAppFavorite.value = favoriteAppsManager.isAppFavorite(AppUtils.getAppNameFromPackageName(context, app.activityInfo.packageName))
                }
            ),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium
    )
}

