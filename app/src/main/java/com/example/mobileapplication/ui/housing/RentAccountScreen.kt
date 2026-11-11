package com.example.mobileapplication.ui.housing

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.RentAccount
import com.example.mobileapplication.data.model.RentPayment
import com.example.mobileapplication.data.repository.RentRepository
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors
import java.time.LocalDate

/**
 * The rent account screen (mockup screen 10).
 *
 * From top to bottom: a prominent "Sample data" notice, the highlighted balance
 * card ("You're up to date" or the amount owed), the next payment with a
 * "Pay rent" button, the payment history and a link to report a housing issue.
 *
 * "Pay rent" never takes a payment: it shows a message explaining that payments
 * aren't available in this prototype.
 *
 * @param state What to show, from [RentViewModel.uiState].
 * @param onPayRent Called when "Pay rent" is tapped.
 * @param onPaymentMessageShown Called once the "payments aren't available" message has been shown.
 * @param onReportIssue Called when "Report a housing issue" is tapped.
 * @param onBack Called when the back arrow is tapped.
 */
@Composable
fun RentAccountScreen(
    state: RentUiState,
    onPayRent: () -> Unit,
    onPaymentMessageShown: () -> Unit,
    onReportIssue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val paymentMessage = stringResource(R.string.rent_payment_unavailable)
    LaunchedEffect(state.showPaymentUnavailable) {
        if (state.showPaymentUnavailable) {
            // showSnackbar waits until the message is dismissed; only then is the flag cleared.
            snackbarHostState.showSnackbar(paymentMessage)
            onPaymentMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.rent_title), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(Dimens.ScreenPadding)
            ) {
                SampleDataNotice()
                BalanceCard(state.account)
                NextPaymentCard(state.account, onPayRent)
                PaymentHistory(state.account.paymentHistory)
                HousingListRow(
                    icon = Icons.Outlined.ReportProblem,
                    title = stringResource(R.string.rent_report_issue),
                    supportingText = null,
                    onClick = onReportIssue,
                    modifier = Modifier.padding(top = Dimens.GroupSpacing)
                )
            }
        }
    }
}

/** The prominent amber notice saying none of this is the user's real rent account. */
@Composable
private fun SampleDataNotice() {
    val colors = MaterialTheme.extendedColors.statusInProgress
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.container, MaterialTheme.shapes.medium)
            .padding(Dimens.CardSpacing)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null, // the text says what this is
            tint = colors.content,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Dimens.GroupSpacing))
        Column {
            Text(
                text = stringResource(R.string.rent_sample_notice_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.content
            )
            Text(
                text = stringResource(R.string.rent_sample_notice_body),
                style = MaterialTheme.typography.bodySmall,
                color = colors.content
            )
        }
    }
}

/**
 * The highlighted (`surfaceTint`) balance card: house icon, the balance in large
 * text, and either a green "You're up to date" with a tick or the amount owed.
 */
@Composable
private fun BalanceCard(account: RentAccount) {
    val balance = formatPence(account.amountOwedPence)
    val statusText = if (account.isUpToDate) {
        stringResource(R.string.rent_up_to_date)
    } else {
        stringResource(R.string.rent_amount_owed, balance)
    }
    val statusColor = if (account.isUpToDate) MaterialTheme.extendedColors.success else MaterialTheme.colorScheme.error
    val cardDescription = stringResource(R.string.rent_balance_description, balance, statusText)

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.surfaceTint,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            // Read the whole card as one sentence, e.g. "Rent balance £0.00. You're up to date."
            .clearAndSetSemantics { contentDescription = cardDescription }
    ) {
        Column(modifier = Modifier.padding(Dimens.ScreenPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIcon(
                    background = MaterialTheme.colorScheme.surface,
                    content = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )
                Spacer(Modifier.weight(1f))
                CircleIcon(
                    background = statusColor,
                    size = 36.dp,
                    content = {
                        Icon(
                            imageVector = if (account.isUpToDate) Icons.Filled.Check else Icons.Filled.PriorityHigh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            }
            Spacer(Modifier.size(Dimens.CardSpacing))
            Text(
                text = balance,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor
            )
        }
    }
}

/** A round background with an icon in the middle. */
@Composable
private fun CircleIcon(
    background: Color,
    size: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .background(background, CircleShape)
    ) {
        content()
    }
}

/** The "Next payment" card with the date, the amount and the coral "Pay rent" button. */
@Composable
private fun NextPaymentCard(account: RentAccount, onPayRent: () -> Unit) {
    val next = account.nextPayment
    AppCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            modifier = Modifier.padding(Dimens.ScreenPadding)
        ) {
            Text(
                text = stringResource(R.string.rent_next_payment),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.semantics { heading() }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(Dimens.GroupSpacing))
                Text(
                    text = formatLongDate(next.dueDate),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatPence(next.amountPence),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            PrimaryButton(text = stringResource(R.string.rent_pay_rent), onClick = onPayRent)
        }
    }
}

/** The "Payment history" heading and a card listing each past payment. */
@Composable
private fun PaymentHistory(payments: List<RentPayment>) {
    Column(modifier = Modifier.padding(top = Dimens.GroupSpacing)) {
        Text(
            text = stringResource(R.string.rent_history_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.rent_history_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    AppCard {
        if (payments.isEmpty()) {
            Text(
                text = stringResource(R.string.rent_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Dimens.ScreenPadding)
            )
            return@AppCard
        }
        Column {
            payments.forEachIndexed { index, payment ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PaymentRow(payment)
            }
        }
    }
}

/** One past payment: date and method on the left, amount on the right. */
@Composable
private fun PaymentRow(payment: RentPayment) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.CardSpacing)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = formatDate(payment.date), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(payment.method.labelRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.rent_history_paid_amount, formatPence(payment.amountPence)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.extendedColors.success
        )
    }
}

/** The fixed date the previews use, so they always look the same. */
private val previewToday = LocalDate.of(2026, 11, 11)

/** Preview of an account that is up to date (as in the mockup). */
@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun RentAccountScreenUpToDatePreview() {
    MobileApplicationTheme {
        RentAccountScreen(
            state = RentUiState(RentRepository.sampleAccount(previewToday)),
            onPayRent = {}, onPaymentMessageShown = {}, onReportIssue = {}, onBack = {}
        )
    }
}

/** Preview of an account with rent owed. */
@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun RentAccountScreenOwedPreview() {
    MobileApplicationTheme {
        RentAccountScreen(
            state = RentUiState(RentRepository.sampleAccount(previewToday, amountOwedPence = 12_050L)),
            onPayRent = {}, onPaymentMessageShown = {}, onReportIssue = {}, onBack = {}
        )
    }
}

/** Preview of an account with no payments yet. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RentAccountScreenNoHistoryPreview() {
    MobileApplicationTheme {
        RentAccountScreen(
            state = RentUiState(RentRepository.sampleAccount(previewToday).copy(paymentHistory = emptyList())),
            onPayRent = {}, onPaymentMessageShown = {}, onReportIssue = {}, onBack = {}
        )
    }
}

/** Preview of the up-to-date account in dark mode. */
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RentAccountScreenDarkPreview() {
    MobileApplicationTheme {
        RentAccountScreen(
            state = RentUiState(RentRepository.sampleAccount(previewToday)),
            onPayRent = {}, onPaymentMessageShown = {}, onReportIssue = {}, onBack = {}
        )
    }
}
