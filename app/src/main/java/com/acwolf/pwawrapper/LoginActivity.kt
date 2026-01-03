package com.acwolf.pwawrapper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

class LoginActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val userField = findViewById<EditText>(R.id.username)
        val passField = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.login_button)
        val errorText = findViewById<TextView>(R.id.error_text)

        loginBtn.setOnClickListener {
            val user = userField.text.toString().trim()
            val pass = passField.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                errorText.text = "All fields required"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // BEST PRACTICE: Use lifecycleScope to manage the background task
            lifecycleScope.launch {
                loginBtn.isEnabled = false // Prevent double clicks
                errorText.visibility = View.GONE

                // Switch to IO thread for the network call
                val result = withContext(Dispatchers.IO) {
                    performLogin(user, pass)
                }

                // Back on Main thread automatically
                loginBtn.isEnabled = true

                if (result.success) {
                    // 1. Save the long-term token for future biometric re-logins
                    // Note: sessionManager.saveSession should accept (token, userId)
                    sessionManager.saveSession(result.token ?: "", result.user_id ?: "")

                    // 2. Pass the SHORT-TERM temp_code to the MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                        // result.temp_code is a String, so the compiler will now correctly
                        // infer putExtra(String, String)
                        putExtra("TEMP_CODE", result.temp_code)

                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    errorText.text = result.message
                    errorText.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun performLogin(user: String, pass: String): LoginResult {
        return try {
            // LOG 1: Check the URL (Verify it isn't localhost)
            Log.d("DEBUG_LOGIN", "1. Connecting to: ${BuildConfig.LOGIN_URL}")

            val url = URL(BuildConfig.LOGIN_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 5000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile")
            conn.setRequestProperty("Content-Type", "application/json")

            val jsonBody = JSONObject().apply {
                put("username", user)
                put("password", pass)
            }

            // LOG 2: Check the Payload
            Log.d("DEBUG_LOGIN", "2. Sending JSON: ${jsonBody.toString()}")

            conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

            val responseCode = conn.responseCode
            Log.d("DEBUG_LOGIN", "3. HTTP Response Code: $responseCode")

            if (responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()

                // LOG 3: The Raw Output (This is the most important one)
                Log.d("DEBUG_LOGIN", "4. Raw Server Output: $response")

                val jsonResponse = JSONObject(response)
                val isSuccess = jsonResponse.optBoolean("success", false)

                if (isSuccess) {
                    val serverToken = jsonResponse.optString("token", "")
                    val tempCode = jsonResponse.optString("temp_code", "") // Extract temp_code
                    val userId = jsonResponse.optString("user_id", "")    // Extract user_id

                    Log.d("DEBUG_LOGIN", "5. Parsed Success - TempCode: $tempCode")

                    // Return the full result
                    LoginResult(
                        success = true,
                        message = "Success",
                        token = serverToken,
                        temp_code = tempCode,
                        user_id = userId)
                } else {
                    LoginResult(false, "Invalid Credentials")
                }
            } else {
                // LOG ERROR: If code isn't 200, see what the error stream says
                val errorBody = conn.errorStream?.bufferedReader()?.readText()
                Log.e("DEBUG_LOGIN", "ERROR BODY: $errorBody")
                LoginResult(false, "Server Error: $responseCode")
            }
        } catch (e: Exception) {
            // LOG EXCEPTION: Check for "Connection Refused" or "Timeout"
            Log.e("DEBUG_LOGIN", "FATAL ERROR: ${e.message}")
            e.printStackTrace()
            LoginResult(false, "Network Error: ${e.localizedMessage}")
        }
    }
    // Simple data class to hold the result
    data class LoginResult(
        val success: Boolean,
        val message: String,
        val token: String? = null,
        val temp_code: String? = null, // Added
        val user_id: String? = null    // Added
    )
}