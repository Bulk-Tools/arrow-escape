package com.bulktools.arrowescape.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("arrow_escape")

/**
 * Persistent game state backed by DataStore preferences.
 *
 * Level stars are stored under keys "stars_{world}_{level}" (int).
 * `unlockedLevel` is a linear level index across all worlds (20 levels per world),
 * i.e. clearing world W level L unlocks index W*20+L+1, capped at 120.
 */
class GameRepository private constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        @Volatile
        private var INSTANCE: GameRepository? = null

        fun getInstance(context: Context): GameRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameRepository(context.applicationContext.dataStore).also {
                    INSTANCE = it
                }
            }
        }

        private const val TOTAL_LEVELS = 120
        private const val LEVELS_PER_WORLD = 20
    }

    private object Keys {
        val unlockedLevel = intPreferencesKey("unlocked_level")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val hapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val themeIndex = intPreferencesKey("theme_index")
        val totalCleared = intPreferencesKey("total_cleared")
        val bestCombo = intPreferencesKey("best_combo")
        val gamesPlayed = intPreferencesKey("games_played")
        val dailyStreak = intPreferencesKey("daily_streak")
        val lastDailyDate = stringPreferencesKey("last_daily_date")
        val totalStars = intPreferencesKey("total_stars")

        fun starsFor(world: Int, level: Int) = intPreferencesKey("stars_${world}_${level}")
    }

    val unlockedLevel: Flow<Int> = dataStore.data.map { it[Keys.unlockedLevel] ?: 0 }
    val soundEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.soundEnabled] ?: true }
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.hapticsEnabled] ?: true }
    val themeIndex: Flow<Int> = dataStore.data.map { it[Keys.themeIndex] ?: 0 }
    val totalCleared: Flow<Int> = dataStore.data.map { it[Keys.totalCleared] ?: 0 }
    val bestCombo: Flow<Int> = dataStore.data.map { it[Keys.bestCombo] ?: 0 }
    val gamesPlayed: Flow<Int> = dataStore.data.map { it[Keys.gamesPlayed] ?: 0 }
    val dailyStreak: Flow<Int> = dataStore.data.map { it[Keys.dailyStreak] ?: 0 }
    val lastDailyDate: Flow<String> = dataStore.data.map { it[Keys.lastDailyDate] ?: "" }
    val totalStars: Flow<Int> = dataStore.data.map { it[Keys.totalStars] ?: 0 }

    fun starsFor(world: Int, level: Int): Flow<Int> =
        dataStore.data.map { it[Keys.starsFor(world, level)] ?: 0 }

    suspend fun setUnlockedLevel(level: Int) {
        dataStore.edit { it[Keys.unlockedLevel] = level }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.soundEnabled] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.hapticsEnabled] = enabled }
    }

    suspend fun setThemeIndex(index: Int) {
        dataStore.edit { it[Keys.themeIndex] = index }
    }

    /** Increments the total-cleared counter by [count]. */
    suspend fun addCleared(count: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.totalCleared] = (prefs[Keys.totalCleared] ?: 0) + count
        }
    }

    /** Keeps the maximum best combo. */
    suspend fun setBestCombo(combo: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.bestCombo] ?: 0
            if (combo > current) prefs[Keys.bestCombo] = combo
        }
    }

    suspend fun incrementGamesPlayed() {
        dataStore.edit { prefs ->
            prefs[Keys.gamesPlayed] = (prefs[Keys.gamesPlayed] ?: 0) + 1
        }
    }

    suspend fun setDailyStreak(streak: Int) {
        dataStore.edit { it[Keys.dailyStreak] = streak }
    }

    suspend fun setLastDailyDate(date: String) {
        dataStore.edit { it[Keys.lastDailyDate] = date }
    }

    /**
     * Saves stars for a level, keeping the max. If [stars] beats the previous best,
     * adds the delta to the total star count. Also advances the unlocked level to
     * at least world*20+level+1, capped at 120 total levels.
     */
    suspend fun saveStars(world: Int, level: Int, stars: Int) {
        val starKey = Keys.starsFor(world, level)
        val nextUnlocked = (world * LEVELS_PER_WORLD + level + 1).coerceAtMost(TOTAL_LEVELS)
        dataStore.edit { prefs ->
            val previous = prefs[starKey] ?: 0
            if (stars > previous) {
                prefs[starKey] = stars
                prefs[Keys.totalStars] = (prefs[Keys.totalStars] ?: 0) + (stars - previous)
            }
            val unlocked = prefs[Keys.unlockedLevel] ?: 0
            if (nextUnlocked > unlocked) prefs[Keys.unlockedLevel] = nextUnlocked
        }
    }

    /** Clears all stored preferences. */
    suspend fun resetAll() {
        dataStore.edit { it.clear() }
    }
}
