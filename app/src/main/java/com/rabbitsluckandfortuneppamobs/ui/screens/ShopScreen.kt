package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.data.Catalog
import com.rabbitsluckandfortuneppamobs.models.ItemType
import com.rabbitsluckandfortuneppamobs.models.PlayerProgress
import com.rabbitsluckandfortuneppamobs.models.ShopItem
import com.rabbitsluckandfortuneppamobs.ui.components.CoinBadge
import com.rabbitsluckandfortuneppamobs.ui.components.FortuneBackground
import com.rabbitsluckandfortuneppamobs.ui.components.ScreenTitle
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRed
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown

/** Shop — virtual coins only, no real-money purchases (spec §5.8, §3.5). */
@Composable
fun ShopScreen(
    progress: PlayerProgress,
    selectedBackground: String,
    onBuy: (ShopItem) -> Unit,
    onEquip: (ShopItem) -> Unit,
    onBack: () -> Unit
) {
    FortuneBackground(selectedBackground = selectedBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = "Shop", coins = progress.totalCoins, onBack = onBack)

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        "Spend your coins on cosmetic items. Everything here is free of real money — coins have no cash value.",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(Catalog.purchasable.size) { index ->
                    val item = Catalog.purchasable[index]
                    val owned = progress.unlockedItems.contains(item.itemId)
                    val equipped = isEquipped(progress, item)
                    ShopRow(
                        item = item,
                        owned = owned,
                        equipped = equipped,
                        canAfford = progress.totalCoins >= item.price,
                        onBuy = { onBuy(item) },
                        onEquip = { onEquip(item) }
                    )
                }
            }
        }
    }
}

fun isEquipped(progress: PlayerProgress, item: ShopItem): Boolean = when (item.itemType) {
    ItemType.CARD_BACK -> progress.selectedCardBack == item.itemId
    ItemType.BACKGROUND -> progress.selectedBackground == item.itemId
    ItemType.RABBIT_SKIN -> progress.selectedRabbitSkin == item.itemId
    ItemType.EFFECT -> false
}

fun itemTypeLabel(type: ItemType): String = when (type) {
    ItemType.CARD_BACK -> "Card Back"
    ItemType.BACKGROUND -> "Background"
    ItemType.RABBIT_SKIN -> "Rabbit Skin"
    ItemType.EFFECT -> "Effect"
}

@Composable
fun TopBar(title: String, coins: Int, onBack: () -> Unit, hideCoins: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        ScreenTitle(title, modifier = Modifier.padding(start = 4.dp))
        Spacer(Modifier.weight(1f))
        if (!hideCoins) {
            CoinBadge(coins = coins, modifier = Modifier.padding(end = 12.dp))
        }
    }
}

@Composable
private fun ShopRow(
    item: ShopItem,
    owned: Boolean,
    equipped: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = FortuneGold, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(item.glyph, fontSize = 26.sp)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkBrown)
                Text(itemTypeLabel(item.itemType), fontSize = 12.sp, color = InkBrown.copy(alpha = 0.7f))
            }

            when {
                equipped -> Text("Equipped", color = FortuneRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                owned -> Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = FortuneRed),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Equip") }
                else -> Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = FortuneGold, contentColor = InkBrown),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("🪙 ${item.price}", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
