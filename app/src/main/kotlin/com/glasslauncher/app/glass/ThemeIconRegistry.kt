package com.glasslauncher.app.glass

import com.glasslauncher.app.R

/**
 * Maps a handful of common package-name patterns to a bundled generic glyph, for the
 * "THEME_CUSTOM" icon render mode. This ships with a small neutral placeholder pack (phone,
 * messages, camera, browser, gallery, clock, mail, settings) — drop branded assets into
 * `res/drawable/theme_icon_*.xml` and add an entry here to extend it.
 */
object ThemeIconRegistry {

    data class ThemeIcon(val id: String, val drawableRes: Int)

    private val icons = listOf(
        ThemeIcon("phone", R.drawable.theme_icon_phone),
        ThemeIcon("message", R.drawable.theme_icon_message),
        ThemeIcon("camera", R.drawable.theme_icon_camera),
        ThemeIcon("browser", R.drawable.theme_icon_browser),
        ThemeIcon("gallery", R.drawable.theme_icon_gallery),
        ThemeIcon("clock", R.drawable.theme_icon_clock),
        ThemeIcon("mail", R.drawable.theme_icon_mail),
        ThemeIcon("settings", R.drawable.theme_icon_settings),
    )

    private val packagePatterns: List<Pair<Regex, String>> = listOf(
        Regex("dialer|contacts|phone") to "phone",
        Regex("messag|sms|mms") to "message",
        Regex("camera") to "camera",
        Regex("chrome|browser|firefox|webview") to "browser",
        Regex("gallery|photos|album") to "gallery",
        Regex("clock|alarm|deskclock") to "clock",
        Regex("gm$|gmail|email|mail") to "mail",
        Regex("settings") to "settings",
    )

    fun allIcons(): List<ThemeIcon> = icons

    fun byId(id: String?): ThemeIcon? = icons.firstOrNull { it.id == id }

    /** Best-effort automatic match for a package name, or null if nothing fits. */
    fun suggestFor(packageName: String): ThemeIcon? {
        val lower = packageName.lowercase()
        val match = packagePatterns.firstOrNull { (regex, _) -> regex.containsMatchIn(lower) } ?: return null
        return byId(match.second)
    }
}
