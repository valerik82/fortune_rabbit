package com.rabbitsluckandfortuneppamobs.ui.navigation

import com.rabbitsluckandfortuneppamobs.models.Difficulty
import com.rabbitsluckandfortuneppamobs.models.GameMode

/** Navigation destinations for the app (spec §13 App Structure). */
object Routes {
    const val SPLASH = "splash"
    const val MENU = "menu"
    const val LEVELS = "levels"
    const val COLLECTION = "collection"
    const val SHOP = "shop"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"

    // game/{mode}/{levelId}/{difficulty}
    const val GAME = "game/{mode}/{levelId}/{difficulty}"

    fun game(mode: GameMode, levelId: Int, difficulty: Difficulty): String =
        "game/${mode.name}/$levelId/${difficulty.name}"
}
