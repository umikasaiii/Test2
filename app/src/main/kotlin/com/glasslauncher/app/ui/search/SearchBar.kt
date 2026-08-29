package com.glasslauncher.app.ui.search

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glasslauncher.app.data.model.SearchBarSettings
import com.glasslauncher.app.glass.GlassSurface

/**
 * The large top search bar. Its behaviour (what a tap/submit does) is driven by
 * [SearchBarSettings.target] via [SearchActions]; app/universal search hands off to the
 * App Drawer's own search field through [onOpenDrawerWithQuery].
 */
@Composable
fun SearchBar(
    settings: SearchBarSettings,
    modifier: Modifier = Modifier,
    onOpenDrawerWithQuery: (String) -> Unit,
) {
    if (!settings.enabled) return
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!text.isNullOrBlank()) {
            query = text
            SearchActions.performTextSearch(context, settings.target, text) { onOpenDrawerWithQuery(text) }
        }
    }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth(settings.widthFraction)
            .height(settings.heightDp.dp),
        cornerRadiusDp = settings.cornerRadiusDp,
        transparencyOverride = settings.transparency,
        blurRadiusDpOverride = settings.blurRadiusDp,
        showGlow = false,
    ) {
        Row(
            Modifier.fillMaxHeight().fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
            Box(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        SearchActions.performTextSearch(context, settings.target, query) {
                            onOpenDrawerWithQuery(query)
                        }
                    },
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                ),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        androidx.compose.material3.Text(
                            text = "Cerca",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 16.sp,
                        )
                    }
                    inner()
                },
            )
            if (settings.showMic) {
                IconButton(onClick = { runCatching { speechLauncher.launch(SearchActions.launchSpeechRecognizer()) } }) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
                }
            }
            if (settings.showLens) {
                IconButton(onClick = { runCatching { context.startActivity(SearchActions.launchVisualSearch(context)) } }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}
