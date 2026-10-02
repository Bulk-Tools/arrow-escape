package com.bulktools.arrowescape.engine

/**
 * Fully describes one level: which world, which level, the board size, the
 * arrow density and special-arrow probabilities, and the RNG seed so the
 * generated board is deterministic. [tutorialIndex] is non-null only for the
 * hand-authored tutorial levels.
 */
data class LevelSpec(
    val world: Int,
    val level: Int,
    val size: Int,
    val density: Double,
    val goldenChance: Double,
    val bombChance: Double,
    val frozenChance: Double,
    val seed: Long,
    val tutorialIndex: Int? = null
)

/**
 * Difficulty parameters shared by every level in one world.
 */
data class WorldDef(
    val name: String,
    val size: Int,
    val density: Double,
    val goldenChance: Double,
    val bombChance: Double,
    val frozenChance: Double
)

/**
 * The game's six worlds and the [LevelSpec] builder.
 */
object Worlds {
    val all = listOf(
        WorldDef("Meadow", 4, 0.45, 0.0, 0.0, 0.0),
        WorldDef("Dunes", 5, 0.5, 0.05, 0.0, 0.0),
        WorldDef("Reef", 6, 0.55, 0.06, 0.04, 0.0),
        WorldDef("Peaks", 7, 0.6, 0.06, 0.05, 0.05),
        WorldDef("Volcano", 8, 0.65, 0.07, 0.06, 0.06),
        WorldDef("Cosmos", 9, 0.7, 0.08, 0.07, 0.07)
    )

    const val LEVELS_PER_WORLD = 20

    /**
     * Returns the [LevelSpec] for the given world and level indices.
     * Worlds 0..5, levels 0..19. The first three levels of world 0 are
     * hand-authored tutorial boards with no randomness.
     */
    fun spec(world: Int, level: Int): LevelSpec {
        require(world in all.indices && level in 0 until LEVELS_PER_WORLD)
        if (world == 0 && level < 3) {
            val sizes = listOf(3, 3, 4)
            return LevelSpec(
                world, level, sizes[level], 0.0, 0.0, 0.0, 0.0,
                world * 1000L + level, tutorialIndex = level
            )
        }
        val w = all[world]
        return LevelSpec(
            world, level, w.size, w.density, w.goldenChance,
            w.bombChance, w.frozenChance, world * 1000L + level
        )
    }
}

/**
 * Hand-authored tutorial boards.
 *
 * T0 (size 3): a single free arrow — tap it to clear the board.
 * T1 (size 3): A at (0,1) pointing RIGHT is blocked by B at (1,1) (A's path
 * hits the occupied cell (1,1)); B at (1,1) pointing UP is free because (1,0)
 * is empty. Intended order: B first, then A.
 * T2 (size 4): A at (0,0) pointing DOWN is blocked by B at (0,1); B at (0,1)
 * pointing RIGHT is blocked by C at (1,1); C at (1,1) pointing DOWN is free
 * because (1,2) and (1,3) are empty. Intended order: C, then B, then A.
 */
object TutorialBoards {
    fun board(index: Int): Map<Cell, Arrow> = when (index) {
        0 -> mapOf(Cell(1, 1) to Arrow(Direction.RIGHT))
        1 -> mapOf(
            Cell(0, 1) to Arrow(Direction.RIGHT),
            Cell(1, 1) to Arrow(Direction.UP)
        )
        else -> mapOf(
            Cell(0, 0) to Arrow(Direction.DOWN),
            Cell(0, 1) to Arrow(Direction.RIGHT),
            Cell(1, 1) to Arrow(Direction.DOWN)
        )
    }
}
