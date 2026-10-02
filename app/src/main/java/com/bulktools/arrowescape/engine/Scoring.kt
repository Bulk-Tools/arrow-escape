package com.bulktools.arrowescape.engine

/**
 * Pure scoring rules.
 */
object Scoring {

    /** Combo multiplier is clamped to [1, MAX_COMBO]. */
    const val MAX_COMBO = 8

    /**
     * Points for a tap that removed [removed] arrows, where [tapped] is the
     * cell the player tapped and [combo] is the current combo level.
     *
     * The tapped arrow is worth 100 x combo (500 x combo if golden); every
     * extra arrow caught in a bomb blast is worth 150 x combo.
     */
    fun pointsFor(removed: List<Pair<Cell, Arrow>>, combo: Int, tapped: Cell): Int {
        val c = combo.coerceIn(1, MAX_COMBO)
        var total = 0
        for ((cell, arrow) in removed) {
            total += if (cell == tapped) {
                (if (arrow.special == Special.GOLDEN) 500 else 100) * c
            } else {
                150 * c
            }
        }
        return total
    }

    /** Bonus for winning with lives to spare. */
    fun winBonus(livesLeft: Int): Int = livesLeft * 200

    /** 3 stars with 3+ lives left, 2 stars with 2, otherwise 1 star. */
    fun starsFor(livesLeft: Int): Int = when {
        livesLeft >= 3 -> 3
        livesLeft == 2 -> 2
        else -> 1
    }
}
