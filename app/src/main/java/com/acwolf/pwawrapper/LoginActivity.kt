package com.acwolf.pwawrapper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("PWA_PREFS", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("IS_LOGGED_IN", false)) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvErrorMessage = findViewById<TextView>(R.id.tvErrorMessage)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                tvErrorMessage.text = "All fields required"
                tvErrorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            thread {
                try {
                    // Endpoint matches your requirement
                    val url = URL(BuildConfig.LOGIN_URL)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    // Create JSON Body
                    val jsonBody = JSONObject().apply {
                        put("username", user)
                        put("password", pass)
                    }

                    conn.outputStream.use { os ->
                        os.write(jsonBody.toString().toByteArray())
                    }

                    if (conn.responseCode == 200) {
                        sharedPref.edit().putBoolean("IS_LOGGED_IN", true).apply()
                        runOnUiThread { navigateToMain() }
                    } else {
                        runOnUiThread {
                            tvErrorMessage.text = "Login failed (${conn.responseCode})"
                            tvErrorMessage.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        tvErrorMessage.text = "Network Error: ${e.localizedMessage}"
                        tvErrorMessage.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}