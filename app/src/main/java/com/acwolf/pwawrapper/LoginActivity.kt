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

        // Session check
        val sharedPref = getSharedPreferences("PWA_PREFS", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("IS_LOGGED_IN", false)) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvErrorMessage = findViewById<TextView>(R.id.tvErrorMessage)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()

            if (username.isEmpty()) {
                tvErrorMessage.text = "Username required"
                tvErrorMessage.visibility = View.VISIBLE
            } else {
                thread {
                    try {
                        // USES GLOBAL URL FROM GRADLE
                        val url = URL("${BuildConfig.BASE_URL}/login")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.connectTimeout = 5000

                        val code = conn.responseCode
                        if (code == 200) {
                            sharedPref.edit().putBoolean("IS_LOGGED_IN", true).apply()
                            runOnUiThread { navigateToMain() }
                        } else {
                            // Parse JSON error from server
                            val errorJson = conn.errorStream.bufferedReader().readText()
                            val msg = JSONObject(errorJson).optString("message", "Login Failed")
                            runOnUiThread {
                                tvErrorMessage.text = msg
                                tvErrorMessage.visibility = View.VISIBLE
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            // YOUR SPECIFIC ERROR MESSAGE
                            tvErrorMessage.text = "Can not connect to ${BuildConfig.BASE_URL}"
                            tvErrorMessage.visibility = View.VISIBLE
                        }
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