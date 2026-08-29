package com.glasslauncher.app.gestures

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object GestureHelper {

    private fun adminComponent(context: Context) = ComponentName(context, LockScreenAdminReceiver::class.java)

    fun isLockAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(adminComponent(context))
    }

    fun requestLockAdminActivation(context: Context): Intent =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Necessario per la gesture \"doppio tap per bloccare lo schermo\".",
            )
        }

    /** Locks the screen now. Only works if [isLockAdminActive] is true. */
    fun lockScreenNow(context: Context): Boolean {
        if (!isLockAdminActive(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return runCatching { dpm.lockNow() }.isSuccess
    }

    /**
     * Best-effort expansion of the notification shade via the hidden StatusBarManager API,
     * exactly what [android.permission.EXPAND_STATUS_BAR] exists for. Some OEM/Android
     * versions may block this via reflection restrictions; failures are silently ignored,
     * since the status bar itself remains swipeable by the user regardless.
     */
    fun expandNotificationShade(context: Context) {
        runCatching {
            val service = context.getSystemService("statusbar")
            val managerClass = Class.forName("android.app.StatusBarManager")
            val method = managerClass.getMethod("expandNotificationsPanel")
            method.invoke(service)
        }
    }
}
