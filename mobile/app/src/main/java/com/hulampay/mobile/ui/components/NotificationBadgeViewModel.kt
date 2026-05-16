package com.hulampay.mobile.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hulampay.mobile.data.preferences.NotificationPreferences
import com.hulampay.mobile.data.state.UnreadCountState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Thin Hilt-injected wrapper that exposes the app-scoped unread-notification
 * count to any composable. Each consuming screen gets its own VM instance,
 * but the underlying [UnreadCountState] is a singleton, so the count stays
 * consistent across screens and updates live from STOMP pushes.
 *
 * Combined with [NotificationPreferences] so the bell badge respects the
 * in-app "Notifications" toggle in Settings — mirrors the website's
 * `localStorage.notificationsEnabled` gating in `shared/context/UnreadContext.jsx`.
 */
@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    private val state: UnreadCountState,
    notificationPreferences: NotificationPreferences,
) : ViewModel() {

    val unread: StateFlow<Long> = combine(state.unread, notificationPreferences.enabled) { count, enabled ->
        if (enabled) count else 0L
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    init {
        state.ensureStarted()
    }
}
