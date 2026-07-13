package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.ui.components.FortuneBackground
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRed
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown

/** Privacy Policy / Terms screen (spec §5.9, §17). Fully offline text. */
@Composable
fun PrivacyPolicyScreen(selectedBackground: String, onBack: () -> Unit) {
    FortuneBackground(selectedBackground = selectedBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = "Privacy Policy", coins = -1, onBack = onBack, hideCoins = true)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Section("Rabbit of Fortune")
                    Body(
                        "Rabbit of Fortune is a relaxing card-matching puzzle game with cute " +
                            "visuals, daily challenges, collectible items, and offline gameplay. " +
                            "It contains no gambling, betting, casino mechanics, or real cash prizes."
                    )

                    Section("Data We Collect")
                    Body(
                        "This app works fully offline. We do not collect, transmit, or share any " +
                            "personal information. All of your game data — completed levels, stars, " +
                            "coins, unlocked items, and settings — is stored only on your device."
                    )

                    Section("Virtual Coins")
                    Body(
                        "Coins in this game are virtual and are used only to unlock cosmetic items " +
                            "inside the app. They have no real-money value and cannot be purchased, " +
                            "exchanged, cashed out, or traded for money or prizes."
                    )

                    Section("Children's Privacy")
                    Body(
                        "The app is family-friendly and does not knowingly collect data from anyone. " +
                            "No account or sign-in is required to play."
                    )

                    Section("Permissions")
                    Body(
                        "The app uses only the vibration feature for gameplay feedback, which can be " +
                            "turned off at any time in Settings."
                    )

                    Section("Terms of Use")
                    Body(
                        "By playing Rabbit of Fortune you agree to use the app for personal, " +
                            "non-commercial entertainment. The app is provided \"as is\" for your enjoyment."
                    )

                    Section("Contact")
                    Body("For questions about this policy, please contact the app publisher.")

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = FortuneRed,
        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
    )
}

@Composable
private fun Body(text: String) {
    Text(text, fontSize = 14.sp, color = InkBrown, lineHeight = 20.sp)
}
