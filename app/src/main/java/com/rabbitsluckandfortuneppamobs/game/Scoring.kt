package com.rabbitsluckandfortuneppamobs.game

import com.rabbitsluckandfortuneppamobs.models.Difficulty
import kotlin.math.max

/**
 * Scoring, stars and coin rewards (spec §3.3, §3.4, §15).
 */
object Scoring {

    const val MATCH_POINTS = 10
    const val COMBO_BONUS = 5
    const val LEVEL_COMPLETE_BONUS = 50
    const val FAST_COMPLETION_BONUS = 20

    /** Seconds under which a level is considered a "fast" completion. */
    private fun fastThreshold(difficulty: Difficulty): Int =
        difficulty.pairs * 8

    /**
     * Live match scoring. [comboStreak] is the number of consecutive matches
     * (without a mismatch in between); each additional one grants a combo bonus.
     */
    fun matchScore(comboStreak: Int): Int =
        MATCH_POINTS + if (comboStreak >= 2) COMBO_BONUS else 0

    /** Final score contribution added once the board is cleared. */
    fun completionScore(difficulty: Difficulty, timeSeconds: Int): Int {
        var bonus = LEVEL_COMPLETE_BONUS
        if (timeSeconds in 1..fastThreshold(difficulty)) {
            bonus += FAST_COMPLETION_BONUS
        }
        return bonus
    }

    /**
     * Stars 1–3 (spec §3.4). Based on how efficiently the board was cleared:
     * the fewer excess moves and the faster the time, the more stars.
     */
    fun calculateStars(difficulty: Difficulty, moves: Int, timeSeconds: Int): Int {
        val perfectMoves = difficulty.pairs           // ideal: one move per pair
        val moveRatio = moves.toDouble() / max(1, perfectMoves)
        val timeRatio = timeSeconds.toDouble() / max(1, fastThreshold(difficulty))

        return when {
            moveRatio <= 1.6 && timeRatio <= 1.2 -> 3
            moveRatio <= 2.4 && timeRatio <= 2.0 -> 2
            else -> 1
        }
    }

    /** Coins earned for a classic/timed level (spec §15). */
    fun coinsForLevel(difficulty: Difficulty, stars: Int): Int {
        val starBonus = when (stars) {
            3 -> 20
            2 -> 10
            else -> 0
        }
        return difficulty.baseReward + starBonus
    }

    /**
     * Daily Challenge reward (spec §15): base +50, plus a streak bonus of
     * +10 per consecutive day capped at +100.
     */
    fun dailyChallengeCoins(streak: Int): Int {
        val streakBonus = (streak * 10).coerceAtMost(100)
        return 50 + streakBonus
    }
}
