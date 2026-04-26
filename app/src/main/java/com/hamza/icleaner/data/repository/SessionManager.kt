package com.hamza.icleaner.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val USER_TOKEN = stringPreferencesKey("user_token")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val userToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TOKEN]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    suspend fun saveSession(token: String, role: String, name: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN] = token
            preferences[USER_ROLE] = role
            preferences[USER_NAME] = name
            preferences[USER_EMAIL] = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
