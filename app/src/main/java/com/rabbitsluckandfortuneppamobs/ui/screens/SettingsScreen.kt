package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.models.PlayerProgress
import com.rabbitsluckandfortuneppamobs.ui.components.FortuneBackground
import com.rabbitsluckandfortuneppamobs.ui.components.ScreenTitle
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRed
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown

/** Settings (spec §5.9). */
@Composable
fun SettingsScreen(
    progress: PlayerProgress,
    appVersion: String,
    onSound: (Boolean) -> Unit,
    onMusic: (Boolean) -> Unit,
    onVibration: (Boolean) -> Unit,
    onReset: () -> Unit,
    onPrivacy: () -> Unit,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    FortuneBackground(selectedBackground = progress.selectedBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = "Settings", coins = progress.totalCoins, onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                SettingsCard {
                    ToggleRow("🔊  Sound Effects", progress.soundEnabled, onSound)
                    HorizontalDivider()
                    ToggleRow("🎵  Music", progress.musicEnabled, onMusic)
                    HorizontalDivider()
                    ToggleRow("📳  Vibration", progress.vibrationEnabled, onVibration)
                }

                Spacer(Modifier.height(16.dp))

                SettingsCard {
                    LinkRow("📄  Privacy Policy", onPrivacy)
                    HorizontalDivider()
                    LinkRow("📜  Terms of Use", onPrivacy)
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ℹ️  App Version", fontSize = 16.sp, color = InkBrown)
                        Text(appVersion, fontSize = 16.sp, color = InkBrown.copy(alpha = 0.7f))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FortuneRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("🗑️  Reset Progress", fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Progress?") },
            text = { Text("This will erase all levels, stars, coins and unlocked items. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onReset()
                }) { Text("Reset", color = FortuneRed) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) { Column(content = content) }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, color = InkBrown)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FortuneGold
            )
        )
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(
                label,
                fontSize = 16.sp,
                color = InkBrown,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
