package com.example.mobileapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.data.model.Session
import com.example.mobileapplication.data.repository.AuthRepository
import com.example.mobileapplication.data.repository.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Whether someone is signed in. [Loading] until the saved session has been read. */
sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val session: Session) : SessionState
}

/**
 * App-wide view of the signed-in session, used by the navigation shell to pick
 * the first screen and to move between Login and Home when the session changes.
 */
class SessionViewModel(sessions: SessionManager) : ViewModel() {

    val state: StateFlow<SessionState> = sessions.session
        .map { session -> if (session == null) SessionState.SignedOut else SessionState.SignedIn(session) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    /** Creates the ViewModel with the app's shared [AuthRepository]. */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SessionViewModel(AuthRepository.getInstance(this[APPLICATION_KEY]!!)) }
        }
    }
}
