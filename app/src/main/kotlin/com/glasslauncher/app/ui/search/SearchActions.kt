package com.glasslauncher.app.ui.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.speech.RecognizerIntent
import com.glasslauncher.app.data.model.SearchTarget
import java.util.Locale

/** Where a tap/submit on the search bar actually goes, driven by [SearchTarget]. */
object SearchActions {

    fun performTextSearch(context: Context, target: SearchTarget, query: String, onFallbackToDrawer: () -> Unit) {
        val trimmed = query.trim()
        when (target) {
            SearchTarget.GOOGLE -> launchGoogleOrWeb(context, trimmed)
            SearchTarget.BROWSER -> launchWeb(context, trimmed)
            SearchTarget.CONTACTS -> launchContacts(context, trimmed)
            SearchTarget.APP_SEARCH, SearchTarget.UNIVERSAL -> onFallbackToDrawer()
        }
    }

    private fun launchGoogleOrWeb(context: Context, query: String) {
        val googleApp = Intent("android.search.action.GLOBAL_SEARCH").apply {
            putExtra("query", query)
            setPackage("com.google.android.googlequicksearchbox")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val resolved = context.packageManager.resolveActivity(googleApp, 0) != null
        if (resolved) {
            runCatching { context.startActivity(googleApp) }.onFailure { launchWeb(context, query) }
        } else {
            launchWeb(context, query)
        }
    }

    private fun launchWeb(context: Context, query: String) {
        val url = if (query.isBlank()) "https://www.google.com" else
            "https://www.google.com/search?q=" + Uri.encode(query)
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun launchContacts(context: Context, @Suppress("UNUSED_PARAMETER") query: String) {
        // The Contacts app provides its own in-app search field; there is no portable way to
        // pre-fill it, so we just bring the app to the front.
        val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    fun launchSpeechRecognizer(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parla per cercare")
    }

    fun launchVisualSearch(context: Context): Intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
