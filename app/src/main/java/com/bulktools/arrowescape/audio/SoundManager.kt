package com.bulktools.arrowescape.audio

import android.content.Context
import android.media.SoundPool
import com.bulktools.arrowescape.R

/**
 * Lightweight singleton for short synthesized UI/game sound effects.
 *
 * Sounds are loaded from `res/raw` once and played via SoundPool.
 * Valid sound names: "pop", "click", "error", "unfreeze", "win", "star".
 * Unknown names are ignored silently.
 */
class SoundManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(6).build()

    private val soundIds: Map<String, Int> = mapOf(
        "pop" to soundPool.load(context, R.raw.pop, 1),
        "click" to soundPool.load(context, R.raw.click, 1),
        "error" to soundPool.load(context, R.raw.error, 1),
        "unfreeze" to soundPool.load(context, R.raw.unfreeze, 1),
        "win" to soundPool.load(context, R.raw.win, 1),
        "star" to soundPool.load(context, R.raw.star, 1)
    )

    /** Master switch; when false, [play] is a no-op. */
    var enabled: Boolean = true

    /** Plays a loaded sound by name; silently ignores unknown names. */
    fun play(name: String) {
        if (!enabled) return
        val id = soundIds[name] ?: return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    /** Releases the underlying SoundPool; the singleton instance stays usable. */
    fun release() {
        soundPool.release()
    }
}
