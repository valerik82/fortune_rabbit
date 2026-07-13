package com.rabbitsluckandfortuneppamobs.models

import kotlinx.serialization.Serializable

/**
 * Card symbols used across the game. Rendered with emoji glyphs so the MVP
 * ships with zero binary art assets while still matching the festive theme
 * described in the spec (§2 Game Concept).
 */
enum class CardSymbol(val glyph: String, val title: String) {
    RABBIT("🐰", "Fortune Rabbit"),
    CARROT("🥕", "Golden Carrot"),
    COIN("🪙", "Lucky Coin"),
    ENVELOPE("🧧", "Red Envelope"),
    LANTERN("🏮", "Lantern"),
    BAMBOO("🎋", "Bamboo"),
    SAKURA("🌸", "Sakura Flower"),
    INGOT("💰", "Golden Ingot"),
    CLOVER("🍀", "Lucky Clover"),
    FIREWORK("🎆", "Firework");

    companion object {
        /** Returns [count] unique symbols using the supplied deterministic RNG. */
        fun pick(count: Int, rng: kotlin.random.Random): List<CardSymbol> =
            entries.shuffled(rng).take(count)
    }
}

/** Difficulty presets from spec §3.2 and timed limits from §4.3. */
enum class Difficulty(
    val displayName: String,
    val pairs: Int,
    val rows: Int,
    val columns: Int,
    val baseReward: Int,   // §15 Rewards Logic
    val timedSeconds: Int  // §4.3 Timed Mode
) {
    EASY("Easy", pairs = 4, rows = 2, columns = 4, baseReward = 20, timedSeconds = 90),
    MEDIUM("Medium", pairs = 6, rows = 3, columns = 4, baseReward = 35, timedSeconds = 120),
    HARD("Hard", pairs = 8, rows = 4, columns = 4, baseReward = 50, timedSeconds = 150),
    EXPERT("Expert", pairs = 10, rows = 4, columns = 5, baseReward = 75, timedSeconds = 180);

    val totalCards: Int get() = pairs * 2
}

/** Play modes from spec §4. */
enum class GameMode { CLASSIC, DAILY_CHALLENGE, TIMED }

/** A single level definition (spec §10 Level). */
data class Level(
    val levelId: Int,
    val difficulty: Difficulty,
    val pairsCount: Int = difficulty.pairs,
    val gridRows: Int = difficulty.rows,
    val gridColumns: Int = difficulty.columns,
    val timeLimit: Int = 0, // 0 = untimed (classic)
    val isUnlocked: Boolean = false
)

/** A card instance on the board. */
data class Card(
    val id: Int,
    val symbol: CardSymbol,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

/** Shop / collection item types (spec §10 ShopItem). */
enum class ItemType { CARD_BACK, BACKGROUND, RABBIT_SKIN, EFFECT }

@Serializable
data class ShopItem(
    val itemId: String,
    val itemType: ItemType,
    val title: String,
    val price: Int,
    val assetName: String,
    val glyph: String = ""
)

/**
 * Persistent player state (spec §10 PlayerProgress, §6 Offline Requirements).
 * Serialized to a single JSON blob inside DataStore.
 */
@Serializable
data class PlayerProgress(
    val totalCoins: Int = 0,
    val completedLevels: Set<Int> = emptySet(),
    val starsPerLevel: Map<Int, Int> = emptyMap(),
    val unlockedItems: Set<String> = DEFAULT_UNLOCKED,
    val selectedCardBack: String = "back_classic",
    val selectedBackground: String = "bg_festive",
    val selectedRabbitSkin: String = "rabbit_classic",
    val dailyChallengeLastDate: String = "",
    val dailyChallengeStreak: Int = 0,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    fun starsFor(levelId: Int): Int = starsPerLevel[levelId] ?: 0

    companion object {
        // Default free items so the player always has a valid selection.
        val DEFAULT_UNLOCKED: Set<String> =
            setOf("back_classic", "bg_festive", "rabbit_classic")
    }
}

/** Result payload shown on the Level Complete screen (spec §5.6). */
data class LevelResult(
    val score: Int,
    val timeSeconds: Int,
    val moves: Int,
    val stars: Int,
    val coinsEarned: Int,
    val streak: Int = 0,
    val isNewBest: Boolean = false
)
