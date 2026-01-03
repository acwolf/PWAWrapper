package com.acwolf.pwawrapper

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// This line MUST be outside the class to work as a property delegate
val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USER_ID_KEY = intPreferencesKey("user_id")

    suspend fun saveSession(token: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId.toIntOrNull() ?: 0
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}