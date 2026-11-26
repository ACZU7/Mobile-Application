package com.example.mobileapplication.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.Role
import com.example.mobileapplication.data.model.Session
import com.example.mobileapplication.data.repository.AuthRepository
import com.example.mobileapplication.data.repository.SessionManager
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The ViewModel for the admin Account tab: who is signed in, and signing out.
 * After signing out the app shell returns to Login.
 */
class AdminAccountViewModel(private val sessions: SessionManager) : ViewModel() {

    /** The signed-in session, or null (briefly) while it loads or after signing out. */
    val session: StateFlow<Session?> =
        sessions.session.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Called when the admin taps Sign out. */
    fun signOut() {
        viewModelScope.launch { sessions.signOut() }
    }

    /** Creates the ViewModel with the app's shared [AuthRepository]. */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { AdminAccountViewModel(AuthRepository.getInstance(this[APPLICATION_KEY]!!)) }
        }
    }
}

/**
 * The admin Account tab: "Signed in as <email> (Admin)", a short demo notice,
 * and Sign out.
 */
@Composable
fun AdminAccountScreen(session: Session?, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.admin_account_title), onBack = null) }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(Dimens.ScreenPadding)
            ) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing),
                        modifier = Modifier.padding(Dimens.ScreenPadding)
                    ) {
                        Text(
                            stringResource(R.string.profile_account_heading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = session?.email?.let {
                                stringResource(R.string.profile_signed_in_as, it, stringResource(R.string.profile_role_admin))
                            }.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        SecondaryButton(
                            text = stringResource(R.string.profile_sign_out),
                            onClick = onSignOut,
                            icon = Icons.AutoMirrored.Filled.Logout
                        )
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.extendedColors.surfaceTint,
                    border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Dimens.CardSpacing)) {
                        Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Dimens.GroupSpacing))
                        Text(stringResource(R.string.admin_account_notice), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/** Signed in as the demo admin. */
@Preview(showBackground = true, heightDp = 500)
@Composable
private fun AdminAccountPreview() = MobileApplicationTheme {
    AdminAccountScreen(Session("admin@example.com", Role.ADMIN), onSignOut = {})
}

/** Dark mode. */
@Preview(showBackground = true, heightDp = 500)
@Composable
private fun AdminAccountDarkPreview() = MobileApplicationTheme(darkTheme = true) {
    AdminAccountScreen(Session("admin@example.com", Role.ADMIN), onSignOut = {})
}
