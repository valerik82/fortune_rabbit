package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.data.Catalog
import com.rabbitsluckandfortuneppamobs.models.PlayerProgress
import com.rabbitsluckandfortuneppamobs.models.ShopItem
import com.rabbitsluckandfortuneppamobs.ui.components.FortuneBackground
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRed
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown

/** Collection — view unlocked items and equip them (spec §5.7). */
@Composable
fun CollectionScreen(
    progress: PlayerProgress,
    selectedBackground: String,
    onEquip: (ShopItem) -> Unit,
    onBack: () -> Unit
) {
    val unlockedCount = Catalog.all.count { progress.unlockedItems.contains(it.itemId) }

    FortuneBackground(selectedBackground = selectedBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = "Collection", coins = progress.totalCoins, onBack = onBack)
            Text(
                "Unlocked $unlockedCount / ${Catalog.all.size} items",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(Catalog.all, key = { it.itemId }) { item ->
                    val owned = progress.unlockedItems.contains(item.itemId)
                    val equipped = isEquipped(progress, item)
                    CollectionCell(
                        item = item,
                        owned = owned,
                        equipped = equipped,
                        onClick = { if (owned) onEquip(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionCell(item: ShopItem, owned: Boolean, equipped: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = owned, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (equipped) FortuneGold else Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    item.glyph,
                    fontSize = 40.sp,
                    modifier = Modifier.alpha(if (owned) 1f else 0.25f)
                )
                if (!owned) {
                    Icon(Icons.Filled.Lock, "Locked", tint = InkBrown.copy(alpha = 0.7f))
                }
                if (equipped) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        "Equipped",
                        tint = FortuneRed,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    )
                }
            }
            Text(
                item.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InkBrown,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = when {
                    equipped -> "Equipped"
                    owned -> "Tap to equip"
                    else -> "🪙 ${item.price}"
                },
                fontSize = 10.sp,
                color = if (owned) FortuneRed else InkBrown.copy(alpha = 0.7f)
            )
        }
    }
}
