package com.glasslauncher.app.data.model

import kotlinx.serialization.Serializable

/** Uniquely identifies an installed app or a shortcut, independent of Android's ComponentName class. */
@Serializable
data class AppKey(
    val packageName: String,
    val activityClassName: String,
    val userHandleHash: Int = 0,
)

@Serializable
data class AppIconOverride(
    val mode: IconRenderMode = IconRenderMode.GLASS_AUTO,
    /** content:// or file:// uri, used only when mode == MANUAL_IMAGE */
    val manualImageUri: String? = null,
    /** id of a bundled theme icon, used only when mode == THEME_CUSTOM */
    val themeIconId: String? = null,
)

/** One cell placed on the Home grid or inside a folder. */
@Serializable
sealed class GridItem {
    abstract val id: String
    abstract val column: Int
    abstract val row: Int

    @Serializable
    data class AppShortcut(
        override val id: String,
        override val column: Int,
        override val row: Int,
        val app: AppKey,
        val iconOverride: AppIconOverride = AppIconOverride(),
        val customLabel: String? = null,
    ) : GridItem()

    @Serializable
    data class Folder(
        override val id: String,
        override val column: Int,
        override val row: Int,
        val name: String,
        val items: List<AppShortcut> = emptyList(),
    ) : GridItem()

    @Serializable
    data class Widget(
        override val id: String,
        override val column: Int,
        override val row: Int,
        val spanColumns: Int,
        val spanRows: Int,
        val kind: WidgetKind,
        /** Only used when kind == THIRD_PARTY */
        val appWidgetId: Int = -1,
    ) : GridItem()
}

@Serializable
enum class WidgetKind { CLOCK, WEATHER, THIRD_PARTY }

@Serializable
data class HomePage(
    val items: List<GridItem> = emptyList(),
)

@Serializable
data class DockItem(
    val slot: Int,
    val app: AppKey,
    val iconOverride: AppIconOverride = AppIconOverride(),
)

@Serializable
data class HomeLayout(
    val pages: List<HomePage> = listOf(HomePage(), HomePage()),
    val dock: List<DockItem> = emptyList(),
    val hiddenApps: Set<AppKey> = emptySet(),
)
