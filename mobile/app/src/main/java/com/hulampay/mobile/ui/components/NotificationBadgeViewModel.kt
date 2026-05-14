package com.hulampay.mobile.ui.components

import androidx.lifecycle.ViewModel
import com.hulampay.mobile.data.state.UnreadCountState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin Hilt-injected wrapper that exposes the app-scoped unread-notification
 * count to any composable. Each consuming screen gets its own VM instance,
 * but the underlying [UnreadCountState] is a singleton, so the count stays
 * consistent across screens and updates live from STOMP pushes.
 */
@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    private val state: UnreadCountState,
) : ViewModel() {

    val unread: StateFlow<Long> = state.unread

    init {
        state.ensureStarted()
    }
}
