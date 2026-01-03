package com.acwolf.pwawrapper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Splash screen must be first
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // 2. Initialize components
        sessionManager = SessionManager(this)
        webView = findViewById(R.id.webview)

        // 3. Configure WebView for "Regular App" behavior
        setupWebView()

        // 4. Handle Android Hardware Back Button
        setupBackNavigation()

        // 5. Route the user based on how they opened the app
        val tempCode = intent.getStringExtra("TEMP_CODE")

        if (!tempCode.isNullOrEmpty()) {
            // Path A: User just logged in via LoginActivity
            executeBridge("code", tempCode)
        } else {
            // Path B: User opened the app normally from the home screen
            handleNormalStart()
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // Standard user agent to avoid "In-app browser" blocks
            userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
/*
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

                // Only allow navigation to your specific domain
                // This stops 500-error redirects or rogue ad-scripts from leaving your app
                return if (url.contains("expensecaptain.com")) {
                    false // False means "Don't override; let the WebView load this"
                } else {
                    Log.e("SECURITY_GATE", "Blocked external redirect to: $url")
                    true // True means "Override; block this navigation"
                }
            }

 */
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // This ensures the PHP session cookie is saved to the phone's disk
                CookieManager.getInstance().flush()
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // go back or exit from home screen
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun handleNormalStart() {
        lifecycleScope.launch {
            val token = sessionManager.getToken()
            if (token.isNullOrEmpty()) {
                navigateToLogin()
            } else {
                // Verified token exists, now ask for fingerprint
                showBiometricPrompt {
                    executeBridge("token", token)
                }
            }
        }
    }

    private fun executeBridge(type: String, value: String) {
        val bridgeUrl = BuildConfig.LOGIN_URL

        // Formats data as "code=12345" or "token=abcdef"
        val postData = "$type=${URLEncoder.encode(value, "UTF-8")}".toByteArray()

        Log.d("BRIDGE_DEBUG", "Executing bridge for $type")
        webView.postUrl(bridgeUrl, postData)
    }
    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If they fail or cancel, we close the app for security
                    finish()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Identity Verification")
            .setSubtitle("Authenticate to access your account")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}