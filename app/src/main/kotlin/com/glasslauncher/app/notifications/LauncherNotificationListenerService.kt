package com.glasslauncher.app.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationEntry(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val icon: Drawable?,
    val title: String,
    val text: String,
    val whenMillis: Long,
    val isOngoing: Boolean,
    val contentIntent: PendingIntent?,
)

/**
 * Real active-notification feed for the glass Notification Panel. Requires the user to grant
 * "Notification access" once (there is no way to auto-grant this — it's a protected, user-only
 * setting), via [requestAccessIntent]. While connected, mirrors the system's live notification
 * list into [notifications] so the panel always reflects real, current notifications.
 */
class LauncherNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        refresh()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance === this) instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) = refresh()
    override fun onNotificationRemoved(sbn: StatusBarNotification) = refresh()

    private fun refresh() {
        val active = runCatching { activeNotifications }.getOrNull() ?: return
        val pm = packageManager
        val entries = active
            .filter { it.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0 }
            .map { sbn ->
                val extras = sbn.notification.extras
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
                val appLabel = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)).toString()
                }.getOrDefault(sbn.packageName)
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: appLabel
                val icon = runCatching { pm.getApplicationIcon(sbn.packageName) }.getOrNull()
                NotificationEntry(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    appLabel = appLabel,
                    icon = icon,
                    title = title,
                    text = text,
                    whenMillis = sbn.postTime,
                    isOngoing = sbn.isOngoing,
                    contentIntent = sbn.notification.contentIntent,
                )
            }
            .sortedByDescending { it.whenMillis }
        _notifications.value = entries
    }

    private fun dismissOne(key: String) {
        runCatching { cancelNotification(key) }
        refresh()
    }

    private fun dismissEverything() {
        runCatching { cancelAllNotifications() }
        refresh()
    }

    companion object {
        private var instance: LauncherNotificationListenerService? = null

        private val _notifications = MutableStateFlow<List<NotificationEntry>>(emptyList())
        val notifications: StateFlow<List<NotificationEntry>> = _notifications.asStateFlow()

        fun isAccessGranted(context: Context): Boolean {
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
            return flat.split(":").any { ComponentName.unflattenFromString(it)?.packageName == context.packageName }
        }

        fun requestAccessIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

        fun dismiss(key: String) = instance?.dismissOne(key) ?: Unit
        fun dismissAll() = instance?.dismissEverything() ?: Unit

        fun open(entry: NotificationEntry) {
            runCatching { entry.contentIntent?.send() }
        }
    }
}
