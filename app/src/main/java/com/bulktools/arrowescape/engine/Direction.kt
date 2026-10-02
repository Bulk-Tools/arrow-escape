package com.bulktools.arrowescape.engine

/**
 * A grid cell identified by its zero-based (x, y) coordinates.
 * x increases to the right, y increases downward (screen coordinates).
 */
data class Cell(val x: Int, val y: Int)

/**
 * The eight compass directions an arrow can point.
 * [angleDeg] is the clockwise rotation in degrees from pointing-right on screen
 * (y axis points down), so UP (0,-1) is 270 degrees.
 */
enum class Direction(val dx: Int, val dy: Int, val angleDeg: Float) {
    RIGHT(1, 0, 0f),
    DOWN_RIGHT(1, 1, 45f),
    DOWN(0, 1, 90f),
    DOWN_LEFT(-1, 1, 135f),
    LEFT(-1, 0, 180f),
    UP_LEFT(-1, -1, 225f),
    UP(0, -1, 270f),
    UP_RIGHT(1, -1, 315f);
}
