package com.rabbitsluckandfortuneppamobs.data

import com.rabbitsluckandfortuneppamobs.models.ItemType
import com.rabbitsluckandfortuneppamobs.models.ShopItem

/**
 * Static catalog of shop / collection items (spec §5.7, §5.8, §17).
 * MVP: 3 card backs and 3 backgrounds are guaranteed; rabbit skins and
 * effects round out the collection. All purchases use virtual coins only.
 */
object Catalog {

    val cardBacks: List<ShopItem> = listOf(
        ShopItem("back_classic", ItemType.CARD_BACK, "Classic Red", 0, "back_classic", "🎴"),
        ShopItem("back_gold", ItemType.CARD_BACK, "Golden Fortune", 150, "back_gold", "🀄"),
        ShopItem("back_lantern", ItemType.CARD_BACK, "Lantern Night", 250, "back_lantern", "🏮"),
    )

    val backgrounds: List<ShopItem> = listOf(
        ShopItem("bg_festive", ItemType.BACKGROUND, "Festive Red", 0, "bg_festive", "🧧"),
        ShopItem("bg_garden", ItemType.BACKGROUND, "Sakura Garden", 200, "bg_garden", "🌸"),
        ShopItem("bg_night", ItemType.BACKGROUND, "Firework Night", 300, "bg_night", "🎆"),
    )

    val rabbitSkins: List<ShopItem> = listOf(
        ShopItem("rabbit_classic", ItemType.RABBIT_SKIN, "Lucky Rabbit", 0, "rabbit_classic", "🐰"),
        ShopItem("rabbit_royal", ItemType.RABBIT_SKIN, "Royal Rabbit", 350, "rabbit_royal", "👑"),
        ShopItem("rabbit_ninja", ItemType.RABBIT_SKIN, "Ninja Rabbit", 400, "rabbit_ninja", "🥷"),
    )

    val effects: List<ShopItem> = listOf(
        ShopItem("fx_sparkle", ItemType.EFFECT, "Sparkle Trail", 300, "fx_sparkle", "✨"),
        ShopItem("fx_confetti", ItemType.EFFECT, "Confetti Burst", 350, "fx_confetti", "🎉"),
    )

    val all: List<ShopItem> = cardBacks + backgrounds + rabbitSkins + effects

    fun byId(id: String): ShopItem? = all.firstOrNull { it.itemId == id }

    /** Items available for purchase in the shop (excludes the free defaults). */
    val purchasable: List<ShopItem> = all.filter { it.price > 0 }
}
