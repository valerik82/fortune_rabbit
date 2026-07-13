package com.rabbitsluckandfortuneppamobs.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabbitsluckandfortuneppamobs.audio.AudioManager
import com.rabbitsluckandfortuneppamobs.data.GameRepository
import com.rabbitsluckandfortuneppamobs.game.LevelGenerator
import com.rabbitsluckandfortuneppamobs.game.Scoring
import com.rabbitsluckandfortuneppamobs.models.Card
import com.rabbitsluckandfortuneppamobs.models.Difficulty
import com.rabbitsluckandfortuneppamobs.models.GameMode
import com.rabbitsluckandfortuneppamobs.models.LevelResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Immutable snapshot of the current game screen state. */
data class GameUiState(
    val cards: List<Card> = emptyList(),
    val moves: Int = 0,
    val elapsedSeconds: Int = 0,
    val timeLimit: Int = 0,           // 0 = untimed
    val timeLeft: Int = 0,
    val score: Int = 0,
    val coins: Int = 0,               // running total for the HUD
    val difficulty: Difficulty = Difficulty.EASY,
    val mode: GameMode = GameMode.CLASSIC,
    val isPaused: Boolean = false,
    val inputLocked: Boolean = false,
    val isComplete: Boolean = false,
    val isFailed: Boolean = false,    // timed-mode timeout
    val result: LevelResult? = null
)

/**
 * Drives a single memory-game session: board flips, matching, timer, scoring
 * (spec §3.1, §3.3, §11, §14). One instance per Game screen.
 */
class GameViewModel(
    private val repository: GameRepository,
    private val audio: AudioManager,
    private val mode: GameMode,
    private val levelId: Int,
    private val difficulty: Difficulty
) : ViewModel() {

    var uiState by mutableStateOf(GameUiState())
        private set

    private var firstPick: Int? = null       // index of the first face-up card
    private var comboStreak = 0
    private var timerStarted = false
    private var timerJob: Job? = null
    private var savedCoins = 0

    init { newGame() }

    fun bindCoins(total: Int) {
        savedCoins = total
        uiState = uiState.copy(coins = total)
    }

    /** Builds a fresh shuffled board and resets all counters (spec §11, §14). */
    fun newGame() {
        timerJob?.cancel()
        timerStarted = false
        firstPick = null
        comboStreak = 0

        val seed = if (mode == GameMode.DAILY_CHALLENGE) LevelGenerator.dailySeed() else null
        val board = LevelGenerator.generateBoard(difficulty, seed)
        val limit = if (mode == GameMode.TIMED) difficulty.timedSeconds else 0

        uiState = GameUiState(
            cards = board,
            timeLimit = limit,
            timeLeft = limit,
            difficulty = difficulty,
            mode = mode,
            coins = savedCoins
        )
    }

    fun onCardTapped(cardId: Int) {
        val state = uiState
        if (state.inputLocked || state.isPaused || state.isComplete || state.isFailed) return
        val card = state.cards.firstOrNull { it.id == cardId } ?: return
        if (card.isFaceUp || card.isMatched) return

        if (!timerStarted) startTimer() // timer starts on first tap (§14)

        audio.play(AudioManager.Sfx.FLIP)
        val flipped = state.cards.map { if (it.id == cardId) it.copy(isFaceUp = true) else it }
        uiState = state.copy(cards = flipped)

        val first = firstPick
        if (first == null) {
            firstPick = cardId
        } else {
            // Second card of the pair.
            uiState = uiState.copy(moves = uiState.moves + 1, inputLocked = true)
            evaluatePair(first, cardId)
        }
    }

    private fun evaluatePair(firstId: Int, secondId: Int) {
        val cards = uiState.cards
        val a = cards.first { it.id == firstId }
        val b = cards.first { it.id == secondId }
        firstPick = null

        if (a.symbol == b.symbol) {
            comboStreak++
            val gained = Scoring.matchScore(comboStreak)
            audio.play(AudioManager.Sfx.MATCH)
            audio.vibrate(25)
            val matched = cards.map {
                if (it.id == firstId || it.id == secondId) it.copy(isMatched = true) else it
            }
            uiState = uiState.copy(
                cards = matched,
                score = uiState.score + gained,
                inputLocked = false
            )
            if (matched.all { it.isMatched }) finishGame()
        } else {
            comboStreak = 0
            audio.play(AudioManager.Sfx.MISMATCH)
            // Flip both back after a short delay (spec §14: 0.8–1.2s).
            viewModelScope.launch {
                delay(1000)
                val reset = uiState.cards.map {
                    if (it.id == firstId || it.id == secondId) it.copy(isFaceUp = false) else it
                }
                uiState = uiState.copy(cards = reset, inputLocked = false)
            }
        }
    }

    private fun startTimer() {
        timerStarted = true
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (uiState.isPaused) continue
                if (uiState.isComplete || uiState.isFailed) break
                if (uiState.timeLimit > 0) {
                    val left = uiState.timeLeft - 1
                    uiState = uiState.copy(
                        timeLeft = left,
                        elapsedSeconds = uiState.elapsedSeconds + 1
                    )
                    if (left <= 0) { failGame(); break }
                } else {
                    uiState = uiState.copy(elapsedSeconds = uiState.elapsedSeconds + 1)
                }
            }
        }
    }

    private fun finishGame() {
        timerJob?.cancel()
        audio.play(AudioManager.Sfx.LEVEL_COMPLETE)
        audio.vibrate(60)

        val time = uiState.elapsedSeconds
        val moves = uiState.moves
        val finalScore = uiState.score + Scoring.completionScore(difficulty, time)
        uiState = uiState.copy(score = finalScore, isComplete = true, inputLocked = true)

        viewModelScope.launch {
            val result = when (mode) {
                GameMode.DAILY_CHALLENGE ->
                    repository.completeDailyChallenge(moves, time, finalScore)
                else ->
                    repository.completeLevel(levelId, difficulty, moves, time, finalScore)
            }
            uiState = uiState.copy(result = result)
        }
    }

    private fun failGame() {
        timerJob?.cancel()
        audio.play(AudioManager.Sfx.MISMATCH)
        uiState = uiState.copy(isFailed = true, inputLocked = true, timeLeft = 0)
    }

    fun pause() { uiState = uiState.copy(isPaused = true) }
    fun resume() { uiState = uiState.copy(isPaused = false) }

    /** Restart resets moves, timer and board (spec §14). */
    fun restart() = newGame()

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
