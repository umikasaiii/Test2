package com.glasslauncher.app.data.model

import android.graphics.drawable.Drawable
import android.os.UserHandle

/** Runtime representation of an installed launchable activity. Not persisted directly. */
data class AppInfo(
    val key: AppKey,
    val label: String,
    val packageName: String,
    val activityClassName: String,
    val userHandle: UserHandle,
    val icon: Drawable,
    val isSystemApp: Boolean = false,
)
