package com.bulktools.arrowescape.engine

/**
 * Special arrow types.
 * - NONE: a plain arrow.
 * - GOLDEN: worth extra points when tapped.
 * - BOMB: when tapped, removes itself and all 8-neighbor arrows, chaining
 *   through neighboring bombs (breadth-first with a visited set).
 * - FROZEN: starts frozen; the first tap on a free frozen arrow unfreezes it
 *   (without removing anything), the next free tap removes it.
 */
enum class Special { NONE, GOLDEN, BOMB, FROZEN }

/**
 * An arrow sitting on the board.
 *
 * @property direction the direction the arrow points (its escape path).
 * @property special the special type of the arrow, defaulting to [Special.NONE].
 * @property frozen true only when [special] is [Special.FROZEN] and the arrow
 * has not been unfrozen yet; tapping a free frozen arrow clears this flag.
 */
data class Arrow(
    val direction: Direction,
    val special: Special = Special.NONE,
    val frozen: Boolean = false
)
