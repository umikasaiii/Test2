package com.glasslauncher.app.glass

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.glasslauncher.app.data.model.GlassMode
import com.glasslauncher.app.data.model.GlassStyleSettings
import com.glasslauncher.app.data.wallpaper.AdaptiveGlassPalette
import dev.chrisbanes.haze.HazeState

/** Whether this device can sustain full Adaptive Glass effects; downgraded on low-RAM hardware. */
object GlassCapability {
    fun isLowPowerDevice(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        return am?.isLowRamDevice == true
    }
}

val LocalGlassStyle: ProvidableCompositionLocal<GlassStyleSettings> =
    compositionLocalOf { GlassStyleSettings() }

val LocalAdaptiveGlassPalette: ProvidableCompositionLocal<AdaptiveGlassPalette> =
    compositionLocalOf { AdaptiveGlassPalette() }

val LocalHazeState: ProvidableCompositionLocal<HazeState> =
    staticCompositionLocalOf { HazeState() }

val LocalGlassLowPower: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

@Composable
fun ProvideGlassEnvironment(
    style: GlassStyleSettings,
    palette: AdaptiveGlassPalette,
    hazeState: HazeState,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lowPower = remember(context) { GlassCapability.isLowPowerDevice(context) }
    CompositionLocalProvider(
        LocalGlassStyle provides style,
        LocalAdaptiveGlassPalette provides palette,
        LocalHazeState provides hazeState,
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
