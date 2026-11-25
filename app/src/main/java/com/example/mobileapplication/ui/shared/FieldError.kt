package com.example.mobileapplication.ui.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

/**
 * An error message shown under a form field, in the same style as the Housing
 * repair form: a warning icon as well as the error colour, so the error is clear
 * even to users who cannot tell colours apart.
 */
@Composable
fun FieldError(message: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.shared_error_icon_description),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
private fun FieldErrorPreview() {
    MobileApplicationTheme {
        FieldError("Enter a valid email address", Modifier.padding(Dimens.ScreenPadding))
    }
}
