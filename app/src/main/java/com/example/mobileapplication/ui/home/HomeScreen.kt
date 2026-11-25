package com.example.mobileapplication.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.ui.shared.ServiceTile
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors
import java.time.LocalDate

/**
 * The Home dashboard (mockup screen 2): a teal header with the Bristol bridge
 * drawing, a time-of-day greeting and a search field; the six service tiles;
 * and the "Your latest repair" card.
 *
 * Stateless: it draws [state] and [query] and reports taps through the callbacks.
 * The whole screen scrolls, so it still works in landscape and with large fonts.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onServiceSelected: (HomeService) -> Unit,
    onLatestRepairSelected: (RepairRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader(state = state, query = query, onQueryChanged = onQueryChanged, onClearQuery = onClearQuery)
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            modifier = Modifier
                .widthIn(max = Dimens.MaxContentWidth)
                .fillMaxWidth()
                .padding(Dimens.ScreenPadding)
        ) {
            if (state.services.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_search_no_results, query.trim()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            } else {
                ServiceGrid(services = state.services, onServiceSelected = onServiceSelected)
            }
            state.latestRepair?.let { LatestRepairCard(it, onClick = { onLatestRepairSelected(it) }) }
        }
    }
}

/**
 * The teal header: greeting, "How can we help you today?", the search field and
 * the bridge drawing. The status bar area above it is teal too (set by the app shell).
 */
@Composable
private fun HomeHeader(
    state: HomeUiState,
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    val greeting = state.firstName?.let { name ->
        when (state.timeOfDay) {
            TimeOfDay.MORNING -> stringResource(R.string.home_greeting_morning, name)
            TimeOfDay.AFTERNOON -> stringResource(R.string.home_greeting_afternoon, name)
            TimeOfDay.EVENING -> stringResource(R.string.home_greeting_evening, name)
        }
    } ?: stringResource(R.string.home_greeting_welcome)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = Dimens.MaxContentWidth)
                .fillMaxWidth()
                .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 24.dp)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                // Hidden (but still taking its space) until the profile is read, so "Welcome" doesn't flash.
                modifier = Modifier
                    .alpha(if (state.isLoaded) 1f else 0f)
                    .semantics { heading() }
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(Dimens.ScreenPadding))
            HomeSearchField(query = query, onQueryChanged = onQueryChanged, onClearQuery = onClearQuery)
            // Decorative drawing of the Clifton Suspension Bridge; screen readers skip it.
            // Left out on short screens (landscape phones) so the tiles stay in view.
            val windowHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
            if (windowHeight >= MinHeightForBridge) Image(
                painter = painterResource(R.drawable.illustration_bristol_bridge),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f)),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BridgeHeight)
                    .padding(top = Dimens.GroupSpacing)
            ) else Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }
}

/** The rounded white search field from the mockup. Typing filters the tiles straight away. */
@Composable
private fun HomeSearchField(query: String, onQueryChanged: (String) -> Unit, onClearQuery: () -> Unit) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = onClearQuery) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.home_search_clear))
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * The tiles in rows of three, as in the mockup. With very large fonts there
 * is not room for three labels side by side, so it drops to two columns.
 * Tiles in the same row share the height of the tallest one.
 */
@Composable
private fun ServiceGrid(services: List<HomeService>, onServiceSelected: (HomeService) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val columns = (maxWidth / (TileWidthPerFontScale * fontScale)).toInt().coerceIn(2, 3)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
            services.chunked(columns).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    row.forEach { service ->
                        ServiceTile(
                            label = stringResource(service.labelRes),
                            icon = service.icon,
                            iconTint = if (service.usesAccent) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            onClick = { onServiceSelected(service) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    // Keep the last row's tiles the same width as the others.
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * "Your latest repair": the newest repair request's status and reference.
 * Tapping it opens that request's progress screen.
 */
@Composable
private fun LatestRepairCard(request: RepairRequest, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 88.dp)
                .padding(Dimens.ScreenPadding)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.extendedColors.surfaceTint, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(Dimens.CardSpacing))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_latest_repair_label), style = MaterialTheme.typography.labelLarge)
                Text(
                    text = stringResource(request.status.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(
                        R.string.home_latest_repair_detail,
                        request.reference,
                        stringResource(request.issueType.labelRes)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (request.isSample) {
                    Text(
                        text = stringResource(R.string.housing_my_repairs_sample_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // AutoMirrored so the chevron points the right way in Arabic (RTL).
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** The tile icons from the mockup. */
private val HomeService.icon: ImageVector
    get() = when (this) {
        HomeService.FIND_PARKING -> Icons.Filled.DirectionsCar
        HomeService.PERMIT_ZONES -> Icons.Filled.LocalParking
        HomeService.REPORT_ISSUE -> Icons.Filled.Warning
        HomeService.CLEAN_AIR_ZONE -> Icons.Filled.Eco
        HomeService.REPORT_REPAIR -> Icons.Filled.Build
        HomeService.RENT_ACCOUNT -> Icons.Filled.Home
    }

/** The mockup draws these two icons in coral (accent); the rest are teal. */
private val HomeService.usesAccent: Boolean
    get() = this == HomeService.REPORT_ISSUE || this == HomeService.RENT_ACCOUNT

/** Screens shorter than this (landscape phones) don't show the bridge drawing. */
private val MinHeightForBridge = 480.dp

/** Height of the bridge drawing under the search field. */
private val BridgeHeight = 104.dp

/** Roughly how wide a tile needs to be per unit of font scale, to fit "Report an issue". */
private val TileWidthPerFontScale = 80.dp

private val previewRepair = RepairRequest(
    reference = "HR-2026-0004",
    room = Room.KITCHEN,
    issueType = IssueType.LEAK,
    description = "Water dripping under the sink.",
    appointmentPreference = AppointmentPreference.MORNING,
    createdDate = LocalDate.of(2026, 11, 20),
    status = RepairStatus.IN_PROGRESS
)

@Composable
private fun HomePreview(state: HomeUiState, query: String = "", darkTheme: Boolean = false) {
    MobileApplicationTheme(darkTheme = darkTheme) {
        HomeScreen(state, query, {}, {}, {}, {})
    }
}

/** As in the mockup: a named profile and a repair in progress. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeNamedPreview() =
    HomePreview(HomeUiState(isLoaded = true, firstName = "Sam", latestRepair = previewRepair))

/** No profile name and no repairs: "Welcome" and no repair card. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeWelcomePreview() = HomePreview(HomeUiState(isLoaded = true, timeOfDay = TimeOfDay.EVENING))

/** Searching for "rent" leaves one tile. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeSearchPreview() = HomePreview(
    HomeUiState(isLoaded = true, firstName = "Sam", services = listOf(HomeService.RENT_ACCOUNT), latestRepair = previewRepair),
    query = "rent"
)

/** A search with no matching tiles. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeNoResultsPreview() =
    HomePreview(HomeUiState(isLoaded = true, firstName = "Sam", services = emptyList()), query = "library")

/** Dark mode, afternoon. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeDarkPreview() = HomePreview(
    HomeUiState(isLoaded = true, timeOfDay = TimeOfDay.AFTERNOON, firstName = "Sam", latestRepair = previewRepair.copy(isSample = true)),
    darkTheme = true
)

/** Large font (200%): the grid drops to two columns. */
@Preview(showBackground = true, heightDp = 1400, fontScale = 2f)
@Composable
private fun HomeLargeFontPreview() =
    HomePreview(HomeUiState(isLoaded = true, firstName = "Sam", latestRepair = previewRepair))

/** Landscape phone. */
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp,orientation=landscape")
@Composable
private fun HomeLandscapePreview() =
    HomePreview(HomeUiState(isLoaded = true, firstName = "Sam", latestRepair = previewRepair))

/** Tablet. */
@Preview(showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
private fun HomeTabletPreview() =
    HomePreview(HomeUiState(isLoaded = true, firstName = "Sam", latestRepair = previewRepair))
