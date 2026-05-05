package com.hulampay.mobile.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val TOKEN_KEY    = stringPreferencesKey("jwt_token")
        private val ROLE_KEY     = stringPreferencesKey("user_role")
        private val USER_JSON_KEY = stringPreferencesKey("user_json")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val role: Flow<String?>  = context.dataStore.data.map { it[ROLE_KEY] }
    val userJson: Flow<String?> = context.dataStore.data.map { it[USER_JSON_KEY] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveRole(role: String) {
        context.dataStore.edit { it[ROLE_KEY] = role }
    }

    suspend fun saveUser(userJson: String) {
        context.dataStore.edit { it[USER_JSON_KEY] = userJson }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(USER_JSON_KEY)
        }
    }
}
