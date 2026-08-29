package com.glasslauncher.app.gestures

import android.app.admin.DeviceAdminReceiver

/**
 * Empty device-admin receiver: its only purpose is to unlock [android.app.admin.DevicePolicyManager.lockNow],
 * which is what powers the optional "double tap to lock" gesture. It requests no other policy.
 */
class LockScreenAdminReceiver : DeviceAdminReceiver()
