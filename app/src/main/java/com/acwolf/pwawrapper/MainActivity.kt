package com.acwolf.pwawrapper

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true // Essential for PWA modern web apps

        // Loads the main site using the injected Build variable
        webView.loadUrl(BuildConfig.WEB_URL)
    }

    // Handle back button so it doesn't close the app immediately
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}