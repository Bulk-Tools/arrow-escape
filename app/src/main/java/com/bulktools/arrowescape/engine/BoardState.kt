package com.bulktools.arrowescape.engine

/**
 * Mutable board state: a [size] x [size] grid of arrows.
 *
 * Arrows are removed by tapping them when their path in [Arrow.direction] is
 * clear of other arrows. Every successful removal is pushed onto [undoStack]
 * so it can be undone.
 */
class BoardState(val size: Int, arrows: Map<Cell, Arrow>) {

    val grid: Array<Array<Arrow?>> = Array(size) { y ->
        Array(size) { x -> arrows[Cell(x, y)] }
    }

    /** History of successful removals, each a list of (cell, arrow) pairs. */
    val undoStack: ArrayDeque<List<Pair<Cell, Arrow>>> = ArrayDeque()

    fun inBounds(cell: Cell) = cell.x in 0 until size && cell.y in 0 until size

    /** Returns the arrow at [cell], or null if the cell is out of bounds or empty. */
    fun arrowAt(cell: Cell): Arrow? = if (inBounds(cell)) grid[cell.y][cell.x] else null

    /**
     * True when every cell strictly along the arrow's ray (excluding its own
     * cell) is empty or out of bounds. Returns false for empty cells.
     */
    fun isFree(cell: Cell): Boolean {
        val arrow = arrowAt(cell) ?: return false
        var x = cell.x + arrow.direction.dx
        var y = cell.y + arrow.direction.dy
        while (x in 0 until size && y in 0 until size) {
            if (grid[y][x] != null) return false
            x += arrow.direction.dx
            y += arrow.direction.dy
        }
        return true
    }

    /**
     * Handles a tap on [cell].
     * - Empty or out-of-bounds cell, or a blocked arrow -> [TapResult.Blocked].
     * - A free frozen arrow is unfrozen (stays on the board) -> [TapResult.Unfrozen].
     * - A free bomb removes itself plus its bomb-chain neighbors -> [TapResult.Removed].
     * - A free plain/golden arrow is removed -> [TapResult.Removed].
     *
     * Successful removals are recorded on the undo stack. [combo] is the
     * current combo level used by [Scoring.pointsFor].
     */
    fun tap(cell: Cell, combo: Int): TapResult {
        val arrow = arrowAt(cell) ?: return TapResult.Blocked(cell)
        if (!isFree(cell)) return TapResult.Blocked(cell)
        if (arrow.frozen) {
            grid[cell.y][cell.x] = arrow.copy(frozen = false)
            return TapResult.Unfrozen(cell)
        }
        val removed = mutableListOf<Pair<Cell, Arrow>>()
        if (arrow.special == Special.BOMB) {
            // Flood-fill through bomb neighbors; the visited set guarantees
            // every cell is queued (and removed) at most once, so chains
            // always terminate.
            val visited = mutableSetOf<Cell>()
            val queue = ArrayDeque<Cell>()
            queue.add(cell)
            visited.add(cell)
            while (queue.isNotEmpty()) {
                val c = queue.removeFirst()
                val a = arrowAt(c) ?: continue
                removed.add(c to a)
                if (a.special == Special.BOMB) {
                    for (dy in -1..1) for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        val n = Cell(c.x + dx, c.y + dy)
                        if (inBounds(n) && arrowAt(n) != null && visited.add(n)) {
                            queue.add(n)
                        }
                    }
                }
            }
        } else {
            removed.add(cell to arrow)
        }
        for ((c, _) in removed) grid[c.y][c.x] = null
        undoStack.addLast(removed)
        return TapResult.Removed(removed.map { it.first }, Scoring.pointsFor(removed, combo, cell))
    }

    /**
     * Restores the most recently removed arrows. Returns false when the
     * undo stack is empty.
     */
    fun undo(): Boolean {
        val snapshot = undoStack.removeLastOrNull() ?: return false
        for ((c, a) in snapshot) if (inBounds(c)) grid[c.y][c.x] = a
        return true
    }

    fun isCleared(): Boolean = grid.all { row -> row.all { it == null } }

    fun remainingCount(): Int = grid.sumOf { row -> row.count { it != null } }

    fun allCells(): List<Pair<Cell, Arrow>> =
        (0 until size).flatMap { y ->
            (0 until size).mapNotNull { x -> grid[y][x]?.let { Cell(x, y) to it } }
        }
}

/** The outcome of tapping a cell. */
sealed interface TapResult {
    /** One or more arrows were removed; [gained] is the points earned. */
    data class Removed(val cells: List<Cell>, val gained: Int) : TapResult
    /** A frozen arrow was unfrozen (it stays on the board). */
    data class Unfrozen(val cell: Cell) : TapResult
    /** Nothing happened: the cell was empty or the arrow's path is blocked. */
    data class Blocked(val cell: Cell) : TapResult
}
