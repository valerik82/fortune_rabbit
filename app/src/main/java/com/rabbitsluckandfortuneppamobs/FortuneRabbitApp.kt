package com.rabbitsluckandfortuneppamobs

import android.app.Application
import com.rabbitsluckandfortuneppamobs.analytics.AnalyticsManager
import com.rabbitsluckandfortuneppamobs.audio.AudioManager
import com.rabbitsluckandfortuneppamobs.data.GameRepository
import com.rabbitsluckandfortuneppamobs.storage.ProgressStore

/**
 * Application container providing the app-wide singletons. Simple manual DI is
 * sufficient for an offline single-module app and keeps the MVP dependency-light.
 */
class FortuneRabbitApp : Application() {

    val repository: GameRepository by lazy { GameRepository(ProgressStore(this)) }
    val audio: AudioManager by lazy { AudioManager(this) }
    val analytics: AnalyticsManager by lazy { AnalyticsManager(this) }

    override fun onCreate() {
        super.onCreate()
        analytics.init()
    }

    override fun onTerminate() {
        super.onTerminate()
        audio.release()
    }
}
