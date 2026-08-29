package com.glasslauncher.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.glasslauncher.app.ui.LauncherApp
import com.glasslauncher.app.ui.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LauncherApp(viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // A singleTask launcher Activity receives every subsequent Home-button press here,
        // rather than a fresh onCreate; always snap back to Home, matching stock launchers.
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.openHome()
        }
    }
}
