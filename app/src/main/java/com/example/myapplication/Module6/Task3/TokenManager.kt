package com.example.myapplication.Module6.Task3

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_bearer_token")
    }

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        context.authDataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return context.authDataStore.data.map { it[TOKEN_KEY] }.firstOrNull()
    }

    suspend fun clearToken() {
        context.authDataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}