package com.glasslauncher.app.glass

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import com.glasslauncher.app.data.model.GlassMode
import com.glasslauncher.app.data.model.GlassStyleSettings
import com.glasslauncher.app.data.wallpaper.AdaptiveGlassPalette

/** Whether this device can sustain full Adaptive Glass effects; downgraded on low-RAM hardware. */
object GlassCapability {
    fun isLowPowerDevice(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        return am?.isLowRamDevice == true
    }

    /** Real-time [androidx.compose.ui.draw.blur] only reliably renders content on API 31+. */
    val supportsRealBlur: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

val LocalGlassStyle: ProvidableCompositionLocal<GlassStyleSettings> =
    compositionLocalOf { GlassStyleSettings() }

val LocalAdaptiveGlassPalette: ProvidableCompositionLocal<AdaptiveGlassPalette> =
    compositionLocalOf { AdaptiveGlassPalette() }

val LocalGlassLowPower: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

/** The live wallpaper bitmap drawn at the root, shared so every [GlassSurface] can draw its own
 * translated, blurred crop of it (there is no way to capture the real OS wallpaper surface). */
val LocalWallpaperBitmap: ProvidableCompositionLocal<ImageBitmap?> = staticCompositionLocalOf { null }

/** The full screen size, used so every [GlassSurface] can size its wallpaper crop identically to
 * the root copy and line pixels up correctly after translation. */
val LocalScreenSizeDp: ProvidableCompositionLocal<DpSize> = staticCompositionLocalOf { DpSize.Zero }

@Composable
fun ProvideGlassEnvironment(
    style: GlassStyleSettings,
    palette: AdaptiveGlassPalette,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lowPower = remember(context) { GlassCapability.isLowPowerDevice(context) }
    CompositionLocalProvider(
        LocalGlassStyle provides style,
        LocalAdaptiveGlassPalette provides palette,
        LocalGlassLowPower provides lowPower,
        content = content,
    )
}

/** Resolves whether the glass should render as light or dark material right now. */
fun GlassStyleSettings.resolvedIsDark(palette: AdaptiveGlassPalette): Boolean = when (mode) {
    GlassMode.LIGHT -> false
    GlassMode.DARK -> true
    GlassMode.AUTO -> palette.isDark
}

/** Applies the low-power downgrade to a blur radius (dp). */
fun GlassStyleSettings.effectiveBlurRadiusDp(lowPower: Boolean): Float =
    if (lowPower) (blurRadiusDp * 0.5f).coerceAtLeast(8f) else blurRadiusDp
