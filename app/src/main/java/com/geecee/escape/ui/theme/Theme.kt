package com.geecee.escape.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import com.geecee.escape.R

private val DarkColorScheme = darkColorScheme(
    primary = foreground,
    secondary = between,
    background = background
)

private val LightColorScheme = lightColorScheme(
    primary = background,
    secondary = between,
    background = foreground
)

@Composable
fun EscapeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val type: Typography
    val locale = Locale.current
    val context = LocalContext.current
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    type = if (locale.language == "ja") {
        //Make font a font that supports japanese
        JPTypography
    } else {
        //Find users preferred font and use it here
        if (sharedPreferencesSettings.getString("font", "jost") == "jost") {
            JostTypography
        }
        else if (sharedPreferencesSettings.getString("font","jost") == "lora"){
            LoraTypography
        }
        else if(sharedPreferencesSettings.getString("font","jost") == "josefin"){
            JosefinTypography
        }
        else{
            JostTypography
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = type,
        content = content
    )

}