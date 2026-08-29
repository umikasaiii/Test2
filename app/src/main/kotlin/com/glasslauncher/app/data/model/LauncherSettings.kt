package com.glasslauncher.app.data.model

import kotlinx.serialization.Serializable

/** Light/Dark/Auto behaviour for the Adaptive Glass material. */
@Serializable
enum class GlassMode { LIGHT, DARK, AUTO }

/** How an app icon is rendered on the Home/Dock/Drawer. */
@Serializable
enum class IconRenderMode { ORIGINAL, GLASS_AUTO, THEME_CUSTOM, MANUAL_IMAGE }

@Serializable
enum class IconShape { SQUIRCLE, CIRCLE, ROUNDED_SQUARE, TEARDROP }

@Serializable
enum class ClockTimeFormat { H12, H24 }

@Serializable
enum class SearchTarget { GOOGLE, BROWSER, APP_SEARCH, CONTACTS, UNIVERSAL }

@Serializable
enum class WeatherSource { AUTO_LOCATION, MANUAL_CITY }

/**
 * Global "Adaptive Glass" material parameters. These drive [com.glasslauncher.app.glass.GlassSurface]
 * everywhere in the app (home, dock, folders, widgets, drawer, settings).
 */
@Serializable
data class GlassStyleSettings(
    val blurRadiusDp: Float = 28f,
    val transparency: Float = 0.22f,
    val opacity: Float = 1f,
    val reflectionIntensity: Float = 0.35f,
    val adaptiveGlassIntensity: Float = 0.65f,
    val edgeHighlightBrightness: Float = 0.55f,
    val shadowIntensity: Float = 0.35f,
    val saturation: Float = 1.05f,
    val cornerRadiusDp: Float = 32f,
    val mode: GlassMode = GlassMode.AUTO,
    val peripheralGlow: Float = 0.18f,
)

@Serializable
data class HomeGridSettings(
    val columns: Int = 5,
    val rows: Int = 7,
    val marginTopDp: Float = 64f,
    val marginBottomDp: Float = 24f,
    val marginHorizontalDp: Float = 16f,
    val horizontalSpacingDp: Float = 12f,
    val verticalSpacingDp: Float = 18f,
    val iconSizeDp: Float = 56f,
    val labelSizeSp: Float = 12f,
    val showLabels: Boolean = true,
    val pageCount: Int = 2,
)

@Serializable
data class IconEngineSettings(
    val defaultRenderMode: IconRenderMode = IconRenderMode.GLASS_AUTO,
    val symbolScale: Float = 0.62f,
    val tileSizeDp: Float = 56f,
    val shape: IconShape = IconShape.SQUIRCLE,
    val cornerRadiusDp: Float = 18f,
    val transparency: Float = 0.28f,
    val glow: Float = 0.3f,
    val borderIntensity: Float = 0.45f,
    val backgroundTint: Float = 0.5f,
    val scale: Float = 1f,
    val paddingDp: Float = 10f,
)

@Serializable
data class DockSettings(
    val enabled: Boolean = true,
    val slotCount: Int = 4,
    val heightDp: Float = 84f,
    val widthFraction: Float = 0.92f,
    val marginBottomDp: Float = 18f,
    val blurRadiusDp: Float = 30f,
    val opacity: Float = 0.9f,
    val cornerRadiusDp: Float = 40f,
    val borderIntensity: Float = 0.5f,
    val iconSizeDp: Float = 52f,
    val iconSpacingDp: Float = 18f,
    val adaptiveReflection: Float = 0.4f,
)

@Serializable
data class FolderSettings(
    val previewGrid: Int = 3, // 2,3,4
    val closedSizeDp: Float = 64f,
    val transparency: Float = 0.3f,
    val blurRadiusDp: Float = 26f,
    val cornerRadiusDp: Float = 28f,
    val paddingDp: Float = 14f,
    val borderIntensity: Float = 0.45f,
    val glow: Float = 0.25f,
)

@Serializable
data class ClockWidgetSettings(
    val enabled: Boolean = true,
    val showDay: Boolean = true,
    val showDate: Boolean = true,
    val timeFormat: ClockTimeFormat = ClockTimeFormat.H24,
    val fontWeight: Int = 600,
    val timeSizeSp: Float = 52f,
    val dayDateSizeSp: Float = 16f,
    val alignment: Int = 1, // 0 start, 1 center, 2 end
    val blurRadiusDp: Float = 24f,
    val transparency: Float = 0.22f,
    val borderIntensity: Float = 0.4f,
    val widthFraction: Float = 0.92f,
    val offsetYDp: Float = 0f,
)

@Serializable
data class WeatherWidgetSettings(
    val enabled: Boolean = true,
    val source: WeatherSource = WeatherSource.AUTO_LOCATION,
    val manualCity: String = "Roma",
    val manualLat: Double = 41.9028,
    val manualLon: Double = 12.4964,
    val useCelsius: Boolean = true,
    val blurRadiusDp: Float = 24f,
    val transparency: Float = 0.22f,
)

@Serializable
data class SearchBarSettings(
    val enabled: Boolean = true,
    val target: SearchTarget = SearchTarget.UNIVERSAL,
    val heightDp: Float = 56f,
    val widthFraction: Float = 0.92f,
    val cornerRadiusDp: Float = 28f,
    val showMic: Boolean = true,
    val showLens: Boolean = true,
    val transparency: Float = 0.22f,
    val blurRadiusDp: Float = 26f,
)

@Serializable
data class AppDrawerSettings(
    val columns: Int = 5,
    val showSearch: Boolean = true,
    val alphabeticalScrollbar: Boolean = true,
    val showRecents: Boolean = true,
    val categoriesEnabled: Boolean = false,
    val blurRadiusDp: Float = 34f,
    val transparency: Float = 0.28f,
)

@Serializable
data class GestureSettings(
    val swipeUpOpensDrawer: Boolean = true,
    val swipeDownOpensNotifications: Boolean = true,
    val doubleTapLocksScreen: Boolean = false,
    val pinchOpensHomeSettings: Boolean = true,
    val longPressOpensEditMode: Boolean = true,
)

@Serializable
data class LauncherSettings(
    val presetName: String = "Glass Reference",
    val glass: GlassStyleSettings = GlassStyleSettings(),
    val homeGrid: HomeGridSettings = HomeGridSettings(),
    val icons: IconEngineSettings = IconEngineSettings(),
    val dock: DockSettings = DockSettings(),
    val folders: FolderSettings = FolderSettings(),
    val clock: ClockWidgetSettings = ClockWidgetSettings(),
    val weather: WeatherWidgetSettings = WeatherWidgetSettings(),
    val search: SearchBarSettings = SearchBarSettings(),
    val drawer: AppDrawerSettings = AppDrawerSettings(),
    val gestures: GestureSettings = GestureSettings(),
) {
    companion object {
        /** The "Glass Reference" preset: reproduces the reference image proportions, without setting its wallpaper. */
        val GLASS_REFERENCE = LauncherSettings()
    }
}
