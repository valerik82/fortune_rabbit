package com.rabbitsluckandfortuneppamobs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabbitsluckandfortuneppamobs.audio.AudioManager
import com.rabbitsluckandfortuneppamobs.data.GameRepository
import com.rabbitsluckandfortuneppamobs.models.PlayerProgress
import com.rabbitsluckandfortuneppamobs.models.ShopItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Activity-scoped ViewModel holding global player progress and mediating
 * shop / settings actions. Keeps the [AudioManager] in sync with saved prefs.
 */
class AppViewModel(
    private val repository: GameRepository,
    private val audio: AudioManager
) : ViewModel() {

    val progress: StateFlow<PlayerProgress> = repository.progress
        .onEach { p -> audio.applySettings(p.soundEnabled, p.musicEnabled, p.vibrationEnabled) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerProgress())

    // ---- Shop / collection ----------------------------------------------

    fun purchase(item: ShopItem, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val ok = repository.purchase(item)
        if (ok) audio.play(AudioManager.Sfx.COIN)
        onResult(ok)
    }

    fun selectItem(item: ShopItem) = viewModelScope.launch {
        audio.play(AudioManager.Sfx.TAP)
        repository.selectItem(item)
    }

    // ---- Settings --------------------------------------------------------

    fun setSound(enabled: Boolean) = viewModelScope.launch { repository.setSound(enabled) }
    fun setMusic(enabled: Boolean) = viewModelScope.launch { repository.setMusic(enabled) }
    fun setVibration(enabled: Boolean) = viewModelScope.launch { repository.setVibration(enabled) }
    fun resetProgress() = viewModelScope.launch { repository.resetProgress() }

    fun tap() = audio.play(AudioManager.Sfx.TAP)
}
