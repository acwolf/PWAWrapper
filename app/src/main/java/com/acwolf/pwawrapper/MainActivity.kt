package com.acwolf.pwawrapper

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var offlineLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Matches your XML ID: android:id="@+id/webview"
        webView = findViewById(R.id.webview)
        offlineLayout = findViewById(R.id.offlineLayout)
        val btnRetry = findViewById<Button>(R.id.btnRetry)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        webView.webViewClient = object : WebViewClient() {
            // For newer devices (API 23+)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                // Only trigger if this is the main frame loading (not just a tiny image failing)
                if (request?.isForMainFrame == true) {
                    showOffline()
                }
            }

            // For older devices
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                showOffline()
            }
        }
        btnRetry.setOnClickListener {
            checkConnectionAndLoad()
        }

        checkConnectionAndLoad()
    }

    private fun checkConnectionAndLoad() {
        if (isNetworkAvailable()) {
            showWeb()
            webView.loadUrl(BuildConfig.WEB_URL)
        } else {
            showOffline()
        }
    }

    private fun showWeb() {
        offlineLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    private fun showOffline() {
        android.widget.Toast.makeText(this, "Switching to Offline View", android.widget.Toast.LENGTH_SHORT).show()
        webView.visibility = View.GONE
        offlineLayout.visibility = View.VISIBLE
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}