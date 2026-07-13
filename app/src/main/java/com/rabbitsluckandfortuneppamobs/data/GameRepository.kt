package com.rabbitsluckandfortuneppamobs.data

import com.rabbitsluckandfortuneppamobs.game.LevelGenerator
import com.rabbitsluckandfortuneppamobs.game.Scoring
import com.rabbitsluckandfortuneppamobs.models.Difficulty
import com.rabbitsluckandfortuneppamobs.models.LevelResult
import com.rabbitsluckandfortuneppamobs.models.PlayerProgress
import com.rabbitsluckandfortuneppamobs.models.ShopItem
import com.rabbitsluckandfortuneppamobs.storage.ProgressStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

/**
 * Central offline repository: the single source of truth for player progress
 * and the gateway for all mutations (completing levels, buying items, settings).
 */
class GameRepository(private val store: ProgressStore) {

    val progress: Flow<PlayerProgress> = store.progressFlow

    val coins: Flow<Int> = progress.map { it.totalCoins }

    // ---- Level completion ------------------------------------------------

    /**
     * Records a finished classic/timed level. Coins are awarded only for the
     * portion improved (stars can be re-earned but coins are granted once at the
     * best star tier to keep the economy fair).
     */
    suspend fun completeLevel(
        levelId: Int,
        difficulty: Difficulty,
        moves: Int,
        timeSeconds: Int,
        score: Int
    ): LevelResult {
        val current = store.progressFlow.first()
        val stars = Scoring.calculateStars(difficulty, moves, timeSeconds)
        val previousStars = current.starsFor(levelId)
        val bestStars = maxOf(stars, previousStars)

        // Award coins for the full level once; on replays only award the
        // difference if the player earned a higher star tier.
        val coinsNow = Scoring.coinsForLevel(difficulty, bestStars)
        val coinsBefore =
            if (current.completedLevels.contains(levelId))
                Scoring.coinsForLevel(difficulty, previousStars)
            else 0
        val coinsEarned = (coinsNow - coinsBefore).coerceAtLeast(0)

        val updated = current.copy(
            totalCoins = current.totalCoins + coinsEarned,
            completedLevels = current.completedLevels + levelId,
            starsPerLevel = current.starsPerLevel + (levelId to bestStars)
        )
        store.save(updated)

        return LevelResult(
            score = score,
            timeSeconds = timeSeconds,
            moves = moves,
            stars = stars,
            coinsEarned = coinsEarned,
            isNewBest = stars > previousStars
        )
    }

    // ---- Daily challenge -------------------------------------------------

    /** Whether the Daily Challenge is still available today. */
    suspend fun isDailyAvailable(): Boolean {
        val today = LevelGenerator.dateKey()
        return store.progressFlow.first().dailyChallengeLastDate != today
    }

    /**
     * Records a completed Daily Challenge, updating the streak (spec §4.2, §15).
     * The streak increments if yesterday was the last completion, otherwise resets.
     */
    suspend fun completeDailyChallenge(
        moves: Int,
        timeSeconds: Int,
        score: Int
    ): LevelResult {
        val current = store.progressFlow.first()
        val today = LevelGenerator.dateKey()
        val yesterday = LevelGenerator.dateKey(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        })

        val alreadyDoneToday = current.dailyChallengeLastDate == today
        if (alreadyDoneToday) {
            // No double rewards; just report a zero-coin result.
            return LevelResult(score, timeSeconds, moves, 3, 0, current.dailyChallengeStreak)
        }

        val newStreak = when (current.dailyChallengeLastDate) {
            yesterday -> current.dailyChallengeStreak + 1
            else -> 1
        }
        val coins = Scoring.dailyChallengeCoins(newStreak - 1)
        val stars = Scoring.calculateStars(LevelGenerator.dailyDifficulty(), moves, timeSeconds)

        val updated = current.copy(
            totalCoins = current.totalCoins + coins,
            dailyChallengeLastDate = today,
            dailyChallengeStreak = newStreak
        )
        store.save(updated)

        return LevelResult(score, timeSeconds, moves, stars, coins, newStreak)
    }

    // ---- Shop ------------------------------------------------------------

    /** Buys an item if affordable and not owned. Returns true on success. */
    suspend fun purchase(item: ShopItem): Boolean {
        val current = store.progressFlow.first()
        if (current.unlockedItems.contains(item.itemId)) return false
        if (current.totalCoins < item.price) return false

        store.save(
            current.copy(
                totalCoins = current.totalCoins - item.price,
                unlockedItems = current.unlockedItems + item.itemId
            )
        )
        return true
    }

    /** Equips an already-unlocked item of the given type. */
    suspend fun selectItem(item: ShopItem) {
        val current = store.progressFlow.first()
        if (!current.unlockedItems.contains(item.itemId)) return
        val updated = when (item.itemType) {
            com.rabbitsluckandfortuneppamobs.models.ItemType.CARD_BACK ->
                current.copy(selectedCardBack = item.itemId)
            com.rabbitsluckandfortuneppamobs.models.ItemType.BACKGROUND ->
                current.copy(selectedBackground = item.itemId)
            com.rabbitsluckandfortuneppamobs.models.ItemType.RABBIT_SKIN ->
                current.copy(selectedRabbitSkin = item.itemId)
            com.rabbitsluckandfortuneppamobs.models.ItemType.EFFECT ->
                current // effects are toggled cosmetics; nothing to equip in MVP
        }
        store.save(updated)
    }

    // ---- Settings --------------------------------------------------------

    suspend fun setSound(enabled: Boolean) =
        mutate { it.copy(soundEnabled = enabled) }

    suspend fun setMusic(enabled: Boolean) =
        mutate { it.copy(musicEnabled = enabled) }

    suspend fun setVibration(enabled: Boolean) =
        mutate { it.copy(vibrationEnabled = enabled) }

    suspend fun resetProgress() = store.reset()

    private suspend fun mutate(block: (PlayerProgress) -> PlayerProgress) {
        store.save(block(store.progressFlow.first()))
    }
}
