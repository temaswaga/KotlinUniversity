package com.example.myapplication.Module5.Task4

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private companion object {
        val HIGHLIGHT_COMPLETED_KEY = booleanPreferencesKey("highlight_completed")
    }

    val highlightCompletedFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[HIGHLIGHT_COMPLETED_KEY] ?: false
        }

    suspend fun setHighlightCompleted(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGHLIGHT_COMPLETED_KEY] = enabled
        }
    }
}