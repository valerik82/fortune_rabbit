package com.rabbitsluckandfortuneppamobs.game

import com.rabbitsluckandfortuneppamobs.models.Card
import com.rabbitsluckandfortuneppamobs.models.CardSymbol
import com.rabbitsluckandfortuneppamobs.models.Difficulty
import com.rabbitsluckandfortuneppamobs.models.GameMode
import com.rabbitsluckandfortuneppamobs.models.Level
import java.util.Calendar
import kotlin.random.Random

/**
 * Builds the level catalog and generates shuffled boards (spec §11).
 */
object LevelGenerator {

    const val TOTAL_LEVELS = 30 // MVP scope, spec §17

    /** Difficulty ramps up as the player progresses through the 30 levels. */
    fun difficultyForLevel(levelId: Int): Difficulty = when {
        levelId <= 8 -> Difficulty.EASY
        levelId <= 16 -> Difficulty.MEDIUM
        levelId <= 24 -> Difficulty.HARD
        else -> Difficulty.EXPERT
    }

    /** Full classic-mode level list with unlock state derived from progress. */
    fun buildLevels(completedLevels: Set<Int>): List<Level> =
        (1..TOTAL_LEVELS).map { id ->
            val diff = difficultyForLevel(id)
            Level(
                levelId = id,
                difficulty = diff,
                timeLimit = 0,
                // Level 1 is always unlocked; each next unlocks after the previous. (§5.3)
                isUnlocked = id == 1 || completedLevels.contains(id - 1)
            )
        }

    /**
     * Generates a shuffled board (spec §11):
     * 1. Select N unique symbols  2. Duplicate each  3. Shuffle  4. Place in grid.
     *
     * @param seed pass a fixed seed for the Daily Challenge so every player gets
     *             the same board offline; pass null for a fresh random board.
     */
    fun generateBoard(difficulty: Difficulty, seed: Long? = null): List<Card> {
        val rng = if (seed != null) Random(seed) else Random(System.nanoTime())
        val symbols: List<CardSymbol> = CardSymbol.pick(difficulty.pairs, rng)
        val deck = (symbols + symbols) // duplicate each symbol
            .shuffled(rng)
        return deck.mapIndexed { index, symbol -> Card(id = index, symbol = symbol) }
    }

    /** Deterministic seed derived from the current date (spec §4.2 / §11). */
    fun dailySeed(calendar: Calendar = Calendar.getInstance()): Long {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        return y * 10000L + m * 100L + d
    }

    /** ISO-like date key (yyyy-MM-dd) used to detect a new day for the streak. */
    fun dateKey(calendar: Calendar = Calendar.getInstance()): String {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }

    /** The Daily Challenge always uses MEDIUM difficulty for a balanced board. */
    fun dailyDifficulty(): Difficulty = Difficulty.MEDIUM
}
