package com.example.mobileapplication.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.shared.FieldError
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.shared.TealButton
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * The Login screen (mockup screen 1): the bridge drawing and app name, email and
 * password fields, a teal "Sign in", "or", an outlined "Continue as guest", and
 * a note listing the prototype's demo accounts.
 *
 * Stateless: it draws [state] and reports actions; [LoginViewModel] does the work.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.extendedColors.surfaceTint)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            modifier = Modifier
                .widthIn(max = Dimens.MaxContentWidth)
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding, vertical = 24.dp)
        ) {
            LoginHeader()
            Spacer(Modifier.height(Dimens.GroupSpacing))
            EmailField(state, onEmailChanged)
            PasswordField(state, onPasswordChanged, onTogglePasswordVisibility, onSignIn)
            state.error?.let { GeneralError(it) }
            TealButton(
                text = stringResource(if (state.isSigningIn) R.string.login_signing_in else R.string.login_sign_in),
                onClick = onSignIn,
                enabled = !state.isSigningIn
            )
            OrDivider()
            SecondaryButton(
                text = stringResource(R.string.login_continue_as_guest),
                onClick = onContinueAsGuest,
                enabled = !state.isSigningIn,
                icon = Icons.Filled.Person
            )
            Spacer(Modifier.height(Dimens.GroupSpacing))
            DemoAccountsNote()
        }
    }
}

/** The bridge drawing, "Bristol City Companion" and the subtitle, in teal. */
@Composable
private fun LoginHeader() {
    // Decorative: the title below names the app.
    Image(
        painter = painterResource(R.drawable.illustration_bristol_bridge),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
    )
    Text(
        text = stringResource(R.string.login_title),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() }
    )
    Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EmailField(state: LoginUiState, onEmailChanged: (String) -> Unit) {
    OutlinedTextField(
        value = state.email,
        onValueChange = onEmailChanged,
        placeholder = { Text(stringResource(R.string.login_email_placeholder)) },
        label = { Text(stringResource(R.string.login_email_label)) },
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        isError = state.emailError != null,
        supportingText = state.emailError?.let { error -> { FieldError(stringResource(error)) } },
        singleLine = true,
        enabled = !state.isSigningIn,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next
        ),
        shape = MaterialTheme.shapes.small,
        colors = loginFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

/** The password field: hidden by default, with an eye button to show it. */
@Composable
private fun PasswordField(
    state: LoginUiState,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignIn: () -> Unit
) {
    OutlinedTextField(
        value = state.password,
        onValueChange = onPasswordChanged,
        placeholder = { Text(stringResource(R.string.login_password_placeholder)) },
        label = { Text(stringResource(R.string.login_password_label)) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    imageVector = if (state.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(
                        if (state.passwordVisible) R.string.login_hide_password else R.string.login_show_password
                    )
                )
            }
        },
        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = state.passwordError != null,
        supportingText = state.passwordError?.let { error -> { FieldError(stringResource(error)) } },
        singleLine = true,
        enabled = !state.isSigningIn,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onSignIn() }),
        shape = MaterialTheme.shapes.small,
        colors = loginFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

/** White fields on the mint background, as in the mockup. */
@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)

/** One message for the whole attempt. Announced by screen readers when it appears. */
@Composable
private fun GeneralError(error: LoginError) {
    val text = when (error) {
        LoginError.InvalidCredentials -> stringResource(R.string.login_error_invalid_credentials)
        is LoginError.LockedOut -> pluralStringResource(R.plurals.login_error_locked_out, error.secondsLeft, error.secondsLeft)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = stringResource(R.string.shared_error_icon_description),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Dimens.GroupSpacing))
        Text(text = text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

/** A line, "or", and a line, between the two ways in. */
@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = stringResource(R.string.login_or),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.CardSpacing)
        )
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** The prototype's sample accounts, clearly labelled as such. */
@Composable
private fun DemoAccountsNote() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(Dimens.CardSpacing)) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(Dimens.GroupSpacing))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.login_demo_title), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.login_demo_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(stringResource(R.string.login_demo_resident), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.login_demo_admin), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LoginPreview(state: LoginUiState, darkTheme: Boolean = false) {
    MobileApplicationTheme(darkTheme = darkTheme) {
        LoginScreen(state, {}, {}, {}, {}, {})
    }
}

/** Empty, as on first launch. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginEmptyPreview() = LoginPreview(LoginUiState())

/** Filled in with the password shown. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginFilledPreview() =
    LoginPreview(LoginUiState(email = "resident@example.com", password = "secret", passwordVisible = true))

/** Field errors after tapping Sign in with a bad email and no password. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginFieldErrorsPreview() = LoginPreview(
    LoginUiState(
        email = "resident@",
        emailError = R.string.login_error_email_format,
        passwordError = R.string.login_error_password_empty
    )
)

/** Wrong email or password. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginInvalidPreview() =
    LoginPreview(LoginUiState(email = "resident@example.com", error = LoginError.InvalidCredentials))

/** Blocked after too many attempts. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginLockedPreview() =
    LoginPreview(LoginUiState(email = "resident@example.com", error = LoginError.LockedOut(30)))

/** While the password is being checked. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginSigningInPreview() =
    LoginPreview(LoginUiState(email = "admin@example.com", password = "secret", isSigningIn = true))

/** Dark mode. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginDarkPreview() = LoginPreview(LoginUiState(), darkTheme = true)

/** Large font (200%). */
@Preview(showBackground = true, heightDp = 1600, fontScale = 2f)
@Composable
private fun LoginLargeFontPreview() = LoginPreview(LoginUiState())

/** Landscape phone. */
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp,orientation=landscape")
@Composable
private fun LoginLandscapePreview() = LoginPreview(LoginUiState())
