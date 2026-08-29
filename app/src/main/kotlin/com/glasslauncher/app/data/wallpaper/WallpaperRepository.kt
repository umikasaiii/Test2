package com.glasslauncher.app.data.wallpaper

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A small, desaturated palette sampled from the current wallpaper, used to tint the glass. */
data class AdaptiveGlassPalette(
    val primary: Color = Color(0xFF9FB4FF),
    val secondary: Color = Color(0xFFD9C08A),
    val tertiary: Color = Color(0xFFC79BE0),
    val isDark: Boolean = true,
)

/**
 * Single owner of "what does the wallpaper look like right now". Provides:
 *  - [palette]: a few sampled colors, refreshed only on wallpaper-change events
 *    (never per-frame), used to tint the Adaptive Glass material.
 *  - [bitmap]: a copy of the live wallpaper drawn into our own Compose tree so that
 *    [dev.chrisbanes.haze] can capture and blur it as a `hazeSource` (the OS wallpaper
 *    surface itself sits outside our window's render tree and cannot be captured directly).
 *
 * Both are cached and only recomputed when the wallpaper actually changes.
 */
class WallpaperRepository(private val context: Context) {

    private val wallpaperManager = WallpaperManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _palette = MutableStateFlow(AdaptiveGlassPalette())
    val palette: StateFlow<AdaptiveGlassPalette> = _palette.asStateFlow()

    private val _bitmap = MutableStateFlow<ImageBitmap?>(null)
    val bitmap: StateFlow<ImageBitmap?> = _bitmap.asStateFlow()

    private var started = false

    private val colorsListener: ((WallpaperColors?, Int) -> Unit)? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            { colors, which -> if (which and WallpaperManager.FLAG_SYSTEM != 0) applyColors(colors) }
        } else null

    private val changedReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) = refreshNow()
    }

    fun start() {
        if (started) return
        started = true
        ContextCompat.registerReceiver(
            context, changedReceiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            colorsListener?.let { wallpaperManager.addOnColorsChangedListener(it, null) }
        }
        refreshNow()
    }

    fun stop() {
        if (!started) return
        started = false
        runCatching { context.unregisterReceiver(changedReceiver) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            colorsListener?.let { runCatching { wallpaperManager.removeOnColorsChangedListener(it) } }
        }
    }

    /** Call right after the user returns from the system wallpaper picker. */
    fun refreshNow() {
        scope.launch {
            reloadPalette()
            reloadBitmap()
        }
    }

    private fun reloadPalette() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val colors = runCatching { wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM) }.getOrNull()
            if (colors != null) {
                applyColors(colors)
                return
            }
        }
        // Legacy / no cached colors yet: derive from the decoded bitmap with Palette.
        val bmp = (runCatching { wallpaperManager.drawable }.getOrNull() as? BitmapDrawable)?.bitmap ?: return
        val scaled = runCatching { Bitmap.createScaledBitmap(bmp, 48, 48, true) }.getOrNull() ?: return
        val result = Palette.from(scaled).generate()
        val dominant = result.getDominantColor(0xFF9FB4FF.toInt())
        val vibrant = result.getVibrantColor(dominant)
        val muted = result.getMutedColor(vibrant)
        val p = Color(dominant)
        val luminance = 0.299f * p.red + 0.587f * p.green + 0.114f * p.blue
        _palette.value = AdaptiveGlassPalette(p, Color(vibrant), Color(muted), isDark = luminance < 0.5f)
    }

    private fun applyColors(colors: WallpaperColors) {
        val primary = Color(colors.primaryColor.toArgb())
        val secondary = colors.secondaryColor?.let { Color(it.toArgb()) } ?: primary
        val tertiary = colors.tertiaryColor?.let { Color(it.toArgb()) } ?: secondary
        val luminance = 0.299f * primary.red + 0.587f * primary.green + 0.114f * primary.blue
        _palette.value = AdaptiveGlassPalette(primary, secondary, tertiary, isDark = luminance < 0.5f)
    }

    private fun reloadBitmap() {
        val drawable = runCatching { wallpaperManager.drawable }.getOrNull() ?: return
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = runCatching {
            val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(b)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
            b
        }.getOrNull() ?: return
        _bitmap.value = bmp.asImageBitmap()
    }
}
