package com.acwolf.pwawrapper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Check if user is already "Logged In"
        val sharedPref = getSharedPreferences("PWA_PREFS", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("IS_LOGGED_IN", false)) {
            // If they are, jump straight to the WebView (MainActivity)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // If not, show the native login form
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etUsername = findViewById<EditText>(R.id.etUsername)

        btnLogin.setOnClickListener {
            if (etUsername.text.isNotEmpty()) {
                // Save session natively
                sharedPref.edit().putBoolean("IS_LOGGED_IN", true).apply()
                // Move to WebView
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}