# Arrow Escape — Game Design

## Core loop

The board is a grid filled with arrows, each pointing in one of 4–8 directions. Tap an arrow to fling it off the board. It moves **only if every cell in front of it, all the way to the edge, is empty** — otherwise it is blocked, flashes red, and (in Classic) costs a life. Clearing an arrow can free the ones behind it, so play is about finding the right order. Clear every arrow to finish the board.

## Modes

- **Classic** — a journey of 120 levels. 3 lives per level; tapping a blocked arrow costs one life. Lose all 3 and the level restarts.
- **Blitz** — 60 seconds on the clock. Boards keep coming; clear as many arrows as possible. No lives — blocked taps just break your combo.
- **Zen** — endless boards, no lives, no timer, no pressure.
- **Daily Challenge** — one board per day, seeded by the date so everyone plays the same puzzle. Completing it extends your daily streak.

## Special arrows

- **Golden** — worth 5× points when cleared.
- **Bomb** — clearing it blasts its 8 neighbors off the board; blasts can chain into other bombs.
- **Frozen** — needs two taps: the first cracks the ice, the second flings it (only the second requires a clear path).

## Scoring

- Clear: **100 × combo** points per arrow.
- Golden: **500 × combo**. Bomb collateral: **150 × combo** per blasted arrow.
- **Combo** rises by 1 with every successful clear (no mistakes between), capped at 8; any blocked tap resets it to 1. Best combo is tracked for stats.
- **Win bonus (Classic)**: remaining lives × 200.
- **Stars (Classic)**: 3 stars for finishing with 3 lives, 2 stars with 2 lives, 1 star with 1 life.

## Levels

6 worlds × 20 levels = 120. Board size and block density grow per world; special-arrow spawn rates per world:

| World | Name    | Size | Density | Golden | Bomb | Frozen |
|------:|---------|-----:|--------:|-------:|-----:|-------:|
| 1     | Meadow  |  4   |  0.45   | 0      | 0    | 0      |
| 2     | Dunes   |  5   |  0.50   | 0.05   | 0    | 0      |
| 3     | Reef    |  6   |  0.55   | 0.06   | 0.04 | 0      |
| 4     | Peaks   |  7   |  0.60   | 0.06   | 0.05 | 0.05   |
| 5     | Volcano |  8   |  0.65   | 0.07   | 0.06 | 0.06   |
| 6     | Cosmos  |  9   |  0.70   | 0.08   | 0.07 | 0.07   |

Levels unlock by earning at least 1 star on the previous level (worlds unlock the same way). A world is *locked* until the last level of the prior world is cleared.

**Tutorial**: World 1 levels 1–3 are scripted: (1) a single arrow with a clear path ("Tap the glowing arrow — its path is clear!"), (2) a blocked arrow first ("That arrow is blocked! Blocked taps cost a life."), (3) an ordering puzzle ("Order matters — clear blockers first.").

**Daily boards** use a seeded RNG from the date (UTC), so every player gets the identical board that day.

**Hints (3 per level)** highlight one currently-free arrow; using a hint does not break combo. **Undo** restores the last move (not available in Blitz).

## Themes

4 color themes: Midnight (default), Sunset, Forest, Candy. Themes recolor the board, arrows, and UI chrome.

## Audio & haptics

Sound effects are synthesized at runtime (no bundled audio files): tap whoosh, blocked buzz, bomb boom, win jingle. Haptics: light tick on clear, double buzz on blocked, heavy pulse on bomb.

## Persistence (DataStore keys)

- `stars.<world>.<level>` — int, 0–3
- `best_score.<world>.<level>` — long
- `stat_cleared`, `stat_best_combo`, `stat_games` — lifetime totals
- `daily_last_played` — epoch-day of last daily attempt; `daily_streak` — current streak
- `pref_sound`, `pref_haptics`, `pref_theme` — settings

## Solvability argument

Every generated board is guaranteed solvable, by reverse construction:

1. Choose a removal order r1..rn for the n arrows.
2. Place arrows in reverse, rn first: each arrow is placed so its straight-line path to its board edge avoids all already-placed arrows (the later-removed ones).
3. Therefore, when arrows r1..r(k−1) have been cleared, rk's path is empty — every arrow becomes free exactly when its predecessors are gone.

Bomb chains and frozen double-taps don't break this: frozen arrows count as two sequential clears of the same cell, and bomb-blasted arrows simply remove some successors early (never creating a new requirement, since every remaining arrow was already freeable in some order).
