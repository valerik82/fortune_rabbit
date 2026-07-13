package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Mirrors the Unity SDK "Server Error... Please try again later" alert with retry. */
@Composable
fun GateErrorScreen(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Server Error") },
        text = { Text("Please try again later") },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("OK")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
