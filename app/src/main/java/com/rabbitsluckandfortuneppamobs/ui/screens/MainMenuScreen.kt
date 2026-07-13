package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.ui.components.CoinBadge
import com.rabbitsluckandfortuneppamobs.ui.components.EmblemCircle
import com.rabbitsluckandfortuneppamobs.ui.components.FortuneBackground
import com.rabbitsluckandfortuneppamobs.ui.components.MenuButton
import com.rabbitsluckandfortuneppamobs.ui.components.NeonGold
import com.rabbitsluckandfortuneppamobs.ui.components.RabbitEmblem
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRedDark
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown

/** Main menu (spec §5.2). */
@Composable
fun MainMenuScreen(
    coins: Int,
    rabbitGlyph: String,
    selectedBackground: String,
    dailyAvailable: Boolean,
    onPlay: () -> Unit,
    onDaily: () -> Unit,
    onCollection: () -> Unit,
    onShop: () -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit
) {
    FortuneBackground(selectedBackground = selectedBackground) {
        CoinBadge(
            coins = coins,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Default skin shows the custom casino rabbit; other skins keep their glyph.
            if (rabbitGlyph == "🐰") RabbitEmblem(size = 118)
            else EmblemCircle(glyph = rabbitGlyph, size = 110)
            Spacer(Modifier.height(12.dp))
            Text(
                "Rabbit of Fortune",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = NeonGold.copy(alpha = 0.8f), blurRadius = 22f)
                )
            )
            Text(
                "A relaxing card-matching puzzle",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            val buttons = Modifier.widthIn(max = 360.dp).fillMaxWidth()
            MenuButton("Play", "▶️", onPlay, buttons)
            MenuButton(
                if (dailyAvailable) "Daily Challenge" else "Daily Done ✓",
                "📅", onDaily, buttons,
                container = if (dailyAvailable) FortuneGold else FortuneRedDark,
                contentColor = if (dailyAvailable) InkBrown else Color.White
            )
            MenuButton("Collection", "🎁", onCollection, buttons, container = FortuneRedDark)
            MenuButton("Shop", "🛒", onShop, buttons, container = FortuneRedDark)
            MenuButton("Settings", "⚙️", onSettings, buttons, container = FortuneRedDark)
            MenuButton("Privacy Policy", "📄", onPrivacy, buttons, container = FortuneRedDark)
        }
    }
}
