package com.hulampay.mobile.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

/**
 * Persists the in-app notification toggle (mirrors the website's
 * `localStorage.notificationsEnabled`). When disabled, the bell badge stays
 * at 0 and STOMP pushes are ignored — matching `shared/context/UnreadContext.jsx`
 * on the web.
 */
@Singleton
class NotificationPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }

    val enabled: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[ENABLED_KEY] ?: true
    }

    suspend fun setEnabled(value: Boolean) {
        context.notificationDataStore.edit { it[ENABLED_KEY] = value }
    }
}
