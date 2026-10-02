package com.bulktools.arrowescape.engine

import kotlin.random.Random

/**
 * Builds solvable boards by reverse construction.
 *
 * ### Solvability argument
 * Let r1..rn be the intended removal order. The generator places arrows in
 * the reverse order, rn down to r1: when rk is placed, only r(k+1)..rn are
 * already on the board, and rk's direction is chosen so its path avoids all
 * of them. During play, when rk is tapped, r1..r(k-1) have already been
 * removed and r(k+1)..rn are not in rk's path, so rk is always free.
 * - Bombs only remove extra neighbors beyond the tapped cell, so they never
 *   make the remaining board unsolvable: any arrow still present still has a
 *   clear path when its turn comes (its blockers are either already gone or
 *   were removed along the way, and removal never adds arrows).
 * - Undo only restores previously removed arrows onto empty cells, so it
 *   cannot introduce a new blockage that the construction did not account for.
 * - Frozen: the first tap on a free frozen arrow unfreezes it without
 *   changing the board (its path is still clear afterwards), and the second
 *   free tap removes it like a normal arrow.
 */
object LevelGenerator {

    /**
     * Generates the arrow map for [spec]. Tutorial specs return their
     * hand-authored board; everything else is generated from the seed.
     */
    fun generate(spec: LevelSpec): Map<Cell, Arrow> {
        spec.tutorialIndex?.let { return TutorialBoards.board(it) }
        return generate(
            spec.size, spec.density, spec.goldenChance,
            spec.bombChance, spec.frozenChance, Random(spec.seed)
        )
    }

    /**
     * Generates [count] = floor(size * size * density) arrows (at least 1) on
     * a [size] x [size] board by reverse construction. Retries up to 200 times
     * with a reshuffled placement order before failing.
     */
    fun generate(
        size: Int,
        density: Double,
        goldenChance: Double,
        bombChance: Double,
        frozenChance: Double,
        random: Random
    ): Map<Cell, Arrow> {
        repeat(200) {
            val count = (size * size * density).toInt().coerceAtLeast(1)
            val cells = (0 until size).flatMap { y -> (0 until size).map { x -> Cell(x, y) } }
            val order = cells.shuffled(random).take(count)
            val placed = mutableMapOf<Cell, Arrow>()
            var ok = true
            // Place in reverse removal order: the first-placed arrow is the
            // last one the player removes.
            for (cell in order.asReversed()) {
                val dirs = Direction.entries
                    .shuffled(random)
                    .filter { d -> pathClear(cell, d, size, placed) }
                if (dirs.isEmpty()) {
                    ok = false
                    break
                }
                val special = rollSpecial(goldenChance, bombChance, frozenChance, random)
                placed[cell] = Arrow(dirs.first(), special, frozen = special == Special.FROZEN)
            }
            if (ok) return placed
        }
        error("Level generation failed")
    }

    /**
     * True when every cell strictly along [dir]'s ray from [cell] is either
     * outside the [size] x [size] board or has no arrow placed in [placed].
     */
    private fun pathClear(cell: Cell, dir: Direction, size: Int, placed: Map<Cell, Arrow>): Boolean {
        var x = cell.x + dir.dx
        var y = cell.y + dir.dy
        while (x in 0 until size && y in 0 until size) {
            if (placed.containsKey(Cell(x, y))) return false
            x += dir.dx
            y += dir.dy
        }
        return true
    }

    private fun rollSpecial(
        goldenChance: Double,
        bombChance: Double,
        frozenChance: Double,
        random: Random
    ): Special {
        val r = random.nextDouble()
        return when {
            r < frozenChance -> Special.FROZEN
            r < frozenChance + goldenChance -> Special.GOLDEN
            r < frozenChance + goldenChance + bombChance -> Special.BOMB
            else -> Special.NONE
        }
    }
}
