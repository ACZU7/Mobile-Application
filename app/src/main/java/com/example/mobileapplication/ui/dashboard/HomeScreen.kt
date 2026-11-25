package com.example.mobileapplication.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.data.model.StreetReport
import com.example.mobileapplication.ui.shared.StatusChip
import com.example.mobileapplication.ui.shared.StatusChipType

private data class Service(val label: Int, val icon: ImageVector, val route: String)

@Composable
fun HomeScreen(reports: List<StreetReport> = emptyList(), onNavigate: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val services = listOf(
        Service(R.string.home_parking, Icons.Default.DirectionsCar, "parking"),
        Service(R.string.home_permit, Icons.Default.LocalParking, "parking/permit"),
        Service(R.string.home_street, Icons.Default.ReportProblem, "streets"),
        Service(R.string.home_caz, Icons.Default.Eco, "cleanairzone"),
        Service(R.string.home_repair, Icons.Default.Build, "housing/form"),
        Service(R.string.home_rent, Icons.Default.Home, "housing/rent-account")
    ).filter { stringResource(it.label).contains(query.trim(), ignoreCase = true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
            Column(Modifier.fillMaxWidth().padding(Dimens.ScreenPadding)) {
                Image(painterResource(R.drawable.illustration_bristol_bridge), null,
                    Modifier.fillMaxWidth().height(96.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary))
                Text(stringResource(R.string.home_greeting), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.home_subtitle))
                OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.home_search)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth())
            }
        }
        services.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                row.forEach { service ->
                    OutlinedCard(onClick = { onNavigate(service.route) }, modifier = Modifier.weight(1f)) {
                        Column(Modifier.fillMaxWidth().heightIn(min = 108.dp).padding(Dimens.GroupSpacing),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(service.icon, null, tint = if (service.label == R.string.home_street)
                                MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                            Text(stringResource(service.label), style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = Dimens.GroupSpacing))
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        if (services.isEmpty()) Text(stringResource(R.string.home_empty))
        OutlinedCard(onClick = { onNavigate("streets/reports") }, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(Dimens.ScreenPadding), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f).padding(horizontal = Dimens.GroupSpacing)) {
                    Text(stringResource(R.string.home_reports))
                    reports.firstOrNull()?.let { report ->
                        StatusChip(stringResource(report.status.labelRes), StatusChipType.valueOf(report.status.name))
                        Text(stringResource(report.category.labelRes), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Icon(Icons.Default.ChevronRight, null)
            }
        }
        Text(stringResource(R.string.home_demo), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ProfileScreen(onStaff: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
        Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineSmall)
        Icon(Icons.Default.AccountCircle, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.profile_guest), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.profile_privacy))
        OutlinedButton(onClick = onStaff, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.staff_entry)) }
        Text(stringResource(R.string.staff_demo), style = MaterialTheme.typography.bodySmall)
    }
}
