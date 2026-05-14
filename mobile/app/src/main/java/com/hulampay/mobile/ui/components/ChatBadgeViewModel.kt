package com.hulampay.mobile.ui.components

import androidx.lifecycle.ViewModel
import com.hulampay.mobile.data.state.ChatUnreadCountState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin Hilt wrapper that exposes the app-scoped unread-chat count to any
 * top-bar composable. Mirrors [NotificationBadgeViewModel].
 */
@HiltViewModel
class ChatBadgeViewModel @Inject constructor(
    private val state: ChatUnreadCountState,
) : ViewModel() {

    val unread: StateFlow<Long> = state.unread

    init {
        state.ensureStarted()
    }
}
