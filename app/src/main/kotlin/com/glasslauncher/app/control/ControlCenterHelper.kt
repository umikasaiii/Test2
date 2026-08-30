package com.glasslauncher.app.control

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings

/**
 * Every quick toggle a Control Center tile can perform. Android increasingly restricts direct
 * hardware control for non-system apps: where a real, silent toggle exists it's used (torch,
 * volume, Do Not Disturb once policy access is granted, brightness/rotation once "modify system
 * settings" is granted); where the platform blocks it entirely for third-party apps (Wi-Fi and
 * Bluetooth radio state since Android 10/13, airplane mode since Android 4.2) this hands off to
 * the appropriate system panel/settings screen instead of pretending to toggle it.
 */
object ControlCenterHelper {

    // ---------- Flashlight (real, no special permission needed for torch-only use) ----------

    private var torchOn = false

    private fun torchCameraId(context: Context): String? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return null
        return runCatching {
            manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }

    fun hasFlashlight(context: Context): Boolean = torchCameraId(context) != null

    fun isTorchOn(): Boolean = torchOn

    fun toggleTorch(context: Context): Boolean {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return torchOn
        val id = torchCameraId(context) ?: return torchOn
        val next = !torchOn
        val ok = runCatching { manager.setTorchMode(id, next) }.isSuccess
        if (ok) torchOn = next
        return torchOn
    }

    // ---------- Volume (real, no special permission) ----------

    fun volumeFraction(context: Context): Float {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max
    }

    fun setVolumeFraction(context: Context, fraction: Float) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (fraction.coerceIn(0f, 1f) * max).toInt()
        runCatching { am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0) }
    }

    // ---------- Brightness (needs "modify system settings" grant) ----------

    fun canWriteSystemSettings(context: Context): Boolean = Settings.System.canWrite(context)

    fun requestWriteSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}"))

    fun brightnessFraction(context: Context): Float = runCatching {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128) / 255f
    }.getOrDefault(0.5f)

    fun setBrightnessFraction(context: Context, fraction: Float) {
        if (!canWriteSystemSettings(context)) return
        val value = (fraction.coerceIn(0f, 1f) * 255).toInt().coerceIn(1, 255)
        runCatching { Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value) }
    }

    // ---------- Rotation lock (needs "modify system settings" grant) ----------

    fun isAutoRotateOn(context: Context): Boolean = runCatching {
        Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1
    }.getOrDefault(true)

    fun toggleAutoRotate(context: Context) {
        if (!canWriteSystemSettings(context)) return
        val next = if (isAutoRotateOn(context)) 0 else 1
        runCatching { Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, next) }
    }

    // ---------- Do Not Disturb (needs Notification Policy Access grant) ----------

    fun hasNotificationPolicyAccess(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun requestNotificationPolicyAccessIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    fun isDndOn(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    fun toggleDnd(context: Context) {
        if (!hasNotificationPolicyAccess(context)) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val next = if (isDndOn(context)) {
            NotificationManager.INTERRUPTION_FILTER_ALL
        } else {
            NotificationManager.INTERRUPTION_FILTER_PRIORITY
        }
        runCatching { nm.setInterruptionFilter(next) }
    }

    // ---------- Wi-Fi: direct toggle is blocked for apps since Android 10; open the system panel ----------

    fun isWifiOn(context: Context): Boolean = runCatching {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wm.isWifiEnabled
    }.getOrDefault(false)

    fun wifiPanelIntent(): Intent =
        if (Build.VERSION.SDK_INT >= 30) Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        else Intent(Settings.ACTION_WIFI_SETTINGS)

    // ---------- Bluetooth: direct enable/disable removed for apps since Android 13; open settings ----------

    fun isBluetoothOn(context: Context): Boolean = runCatching {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter?.isEnabled == true
    }.getOrDefault(false)

    fun bluetoothSettingsIntent(): Intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

    // ---------- Airplane mode: no app can toggle this since Android 4.2; open settings ----------

    fun isAirplaneModeOn(context: Context): Boolean = runCatching {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }.getOrDefault(false)

    fun airplaneModeSettingsIntent(): Intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
}
