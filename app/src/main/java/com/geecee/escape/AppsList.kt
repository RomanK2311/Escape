package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    packageManager: PackageManager,
    context: Context,
    onCloseAppDrawer: () -> Unit,
    favoriteAppsManager: FavoriteAppsManager
) {
    val haptics = LocalHapticFeedback.current
    val installedApps = packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.GET_ACTIVITIES
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var currentSelectedApp by remember { mutableStateOf("") }
    var currentPackageName by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(favoriteAppsManager.isAppFavorite(currentPackageName)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(10.dp, 50.dp, 10.dp, 0.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(0.dp, 0.dp, 0.dp, 140.dp)
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "All Apps", color = MaterialTheme.colorScheme.primary, fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            installedApps.sortedBy { it.loadLabel(packageManager).toString() }.forEach { appInfo ->
                Text(
                    appInfo.loadLabel(packageManager).toString(),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent =
                                packageManager.getLaunchIntentForPackage(appInfo.activityInfo.packageName)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeCustomAnimation(
                                    context, R.anim.slide_in_bottom, R.anim.slide_out_top
                                )
                                context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showBottomSheet = true
                            currentSelectedApp = appInfo
                                .loadLabel(packageManager)
                                .toString()
                            currentPackageName = appInfo.activityInfo.packageName
                            isFavorite = favoriteAppsManager.isAppFavorite(currentPackageName)
                        }),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 50.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Button(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onCloseAppDrawer()
        }) {
            Icon(Icons.Rounded.KeyboardArrowUp, "Open app drawer", tint = Color.Black)
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false; }, sheetState = sheetState) {
            Column(Modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
                Row {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "App Options",
                        tint = Color.White,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(0.dp, 0.dp, 10.dp, 0.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        currentSelectedApp,
                        Modifier,
                        MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp
                    )
                }
                HorizontalDivider(Modifier.padding(0.dp, 15.dp))
                Column(Modifier.padding(47.dp, 0.dp, 0.dp, 0.dp)) {
                    Text(
                        "Uninstall",
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                // Uninstall logic here
                                val intent =
                                    Intent(
                                        Intent.ACTION_DELETE,
                                        Uri.parse("package:$currentPackageName")
                                    )
                                Log.d("PACKAGE NAME", "package:$currentPackageName")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                    Text(
                        "Hide",
                        Modifier.padding(0.dp, 10.dp),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                    Text(
                        text = if (isFavorite) "Remove from favourites" else "Add to favourites",
                        modifier = Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                if (isFavorite) {
                                    favoriteAppsManager.removeFavoriteApp(currentPackageName)
                                } else {
                                    favoriteAppsManager.addFavoriteApp(currentPackageName)
                                }
                                // Update the state after the operation
                                isFavorite = favoriteAppsManager.isAppFavorite(currentPackageName)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                    Text(
                        "Add Open Challenge",
                        Modifier.padding(0.dp, 10.dp),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                    Text(
                        "App Info",
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                val Intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:$currentPackageName")
                                    }
                                context.startActivity(Intent)
                                showBottomSheet = false
                            }),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                }
            }
        }
    }
}