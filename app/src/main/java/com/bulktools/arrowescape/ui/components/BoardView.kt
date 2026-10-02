package com.bulktools.arrowescape.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import com.bulktools.arrowescape.engine.Arrow
import com.bulktools.arrowescape.engine.Cell
import com.bulktools.arrowescape.theme.ThemeDef
import kotlin.math.PI
import kotlin.math.sin

data class DispArrow(
    val arrow: Arrow,
    var removing: Boolean = false,
    var removeProgress: Float = 0f,
    var shakeKey: Int = 0,
    var hintPulse: Boolean = false
)

data class Particle(
    val cell: Cell,
    var ox: Float,
    var oy: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color
)

data class ScorePopup(
    val cell: Cell,
    val text: String,
    var life: Float
)

data class ConfettiPiece(
    var x: Float,
    var y: Float,
    var vy: Float,
    var rot: Float,
    var vrot: Float,
    var life: Float,
    val color: Color
)

@Composable
fun BoardView(
    size: Int,
    displayGrid: SnapshotStateMap<Cell, DispArrow>,
    def: ThemeDef,
    onCellTap: (Cell) -> Unit,
    particles: List<Particle>,
    popups: List<ScorePopup>,
    confetti: List<ConfettiPiece>,
    shakeOffsets: SnapshotStateMap<Cell, Float> = remember { mutableStateMapOf() },
    modifier: Modifier = Modifier
) {
    val gridN = size
    val tapHandler by rememberUpdatedState(onCellTap)
    val textMeasurer = rememberTextMeasurer()
    var tick by remember { mutableStateOf(0L) }
    // Plain (non-State) elapsed seconds; read during draw, advanced by the ticker.
    val elapsedSec = remember { floatArrayOf(0f) }

    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
            last = now
            elapsedSec[0] += dt
            var alive = false
            (particles as? MutableList<Particle>)?.let { list ->
                val iter = list.iterator()
                while (iter.hasNext()) {
                    val p = iter.next()
                    p.life -= dt * 1.4f
                    if (p.life <= 0f) {
                        iter.remove()
                    } else {
                        p.ox += p.vx * dt
                        p.oy += p.vy * dt
                        p.vy += 2.5f * dt
                    }
                }
                if (list.isNotEmpty()) alive = true
            }
            (popups as? MutableList<ScorePopup>)?.let { list ->
                val iter = list.iterator()
                while (iter.hasNext()) {
                    val p = iter.next()
                    p.life -= dt * 0.8f
                    if (p.life <= 0f) iter.remove()
                }
                if (list.isNotEmpty()) alive = true
            }
            (confetti as? MutableList<ConfettiPiece>)?.let { list ->
                val iter = list.iterator()
                while (iter.hasNext()) {
                    val c = iter.next()
                    c.y += c.vy * dt
                    c.rot += c.vrot * dt
                    c.life -= dt * 0.35f
                    if (c.y > 1.25f || c.life <= 0f) iter.remove()
                }
                if (list.isNotEmpty()) alive = true
            }
            if (!alive && displayGrid.values.any { it.hintPulse }) alive = true
            if (alive) tick++
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val boardPx = with(LocalDensity.current) { maxWidth.toPx() }
        val tile = if (gridN > 0) boardPx / gridN else 0f

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(gridN, tile) {
                    detectTapGestures { offset ->
                        if (tile <= 0f) return@detectTapGestures
                        val x = (offset.x / tile).toInt().coerceIn(0, gridN - 1)
                        val y = (offset.y / tile).toInt().coerceIn(0, gridN - 1)
                        tapHandler(Cell(x, y))
                    }
                }
        ) {
            @Suppress("UNUSED_EXPRESSION")
            tick
            if (tile <= 0f) return@Canvas
            val pulse = 0.55f + 0.45f * sin(elapsedSec[0] * 5f)
            val pad = tile * 0.045f
            val tileR = tile * 0.18f
            val borderColor = Color.White.copy(alpha = 0.07f)

            for (gy in 0 until gridN) {
                for (gx in 0 until gridN) {
                    val tl = Offset(gx * tile + pad, gy * tile + pad)
                    val ts = tile - pad * 2f
                    drawRoundRect(
                        color = def.tile,
                        topLeft = tl,
                        size = Size(ts, ts),
                        cornerRadius = CornerRadius(tileR, tileR)
                    )
                    drawRoundRect(
                        color = borderColor,
                        topLeft = tl,
                        size = Size(ts, ts),
                        cornerRadius = CornerRadius(tileR, tileR),
                        style = Stroke(width = (tile * 0.012f).coerceAtLeast(1f))
                    )
                }
            }

            displayGrid.forEach { (cell, disp) ->
                val center = Offset((cell.x + 0.5f) * tile, (cell.y + 0.5f) * tile)
                val shake = shakeOffsets[cell] ?: 0f
                val sx = if (shake > 0f) sin(shake * 4f * PI.toFloat()) * shake * tile * 0.12f else 0f
                if (shake > 0f) {
                    drawCircle(
                        color = Color.Red.copy(alpha = 0.35f * shake.coerceIn(0f, 1f)),
                        radius = tile * 0.42f,
                        center = center
                    )
                }
                val c = center + Offset(sx, 0f)
                if (disp.removing) {
                    val p = disp.removeProgress.coerceIn(0f, 1f)
                    val d = disp.arrow.direction
                    withTransform({
                        translate(d.dx * p * tile * 0.9f, d.dy * p * tile * 0.9f)
                    }) {
                        drawArrowRaw(this, disp.arrow, def, c, tile * 0.62f, 1f - p)
                    }
                } else {
                    if (disp.hintPulse) {
                        drawCircle(
                            color = def.gold.copy(alpha = 0.55f * pulse),
                            radius = tile * 0.46f,
                            center = c,
                            style = Stroke(width = (tile * 0.03f).coerceAtLeast(2f))
                        )
                    }
                    drawArrowRaw(this, disp.arrow, def, c, tile * 0.62f, 1f)
                }
            }

            particles.forEach { p ->
                val a = p.life.coerceIn(0f, 1f)
                drawCircle(
                    color = p.color.copy(alpha = a),
                    radius = tile * 0.035f * (0.6f + 0.4f * a) + 1f,
                    center = Offset(
                        (p.cell.x + 0.5f + p.ox) * tile,
                        (p.cell.y + 0.5f + p.oy) * tile
                    )
                )
            }

            popups.forEach { p ->
                val a = p.life.coerceIn(0f, 1f)
                val cx = (p.cell.x + 0.5f) * tile
                val cy = (p.cell.y + 0.5f) * tile - (1f - p.life) * tile * 0.8f
                drawText(
                    textMeasurer = textMeasurer,
                    text = p.text,
                    topLeft = Offset(cx - tile * 0.35f, cy - tile * 0.2f),
                    style = TextStyle(
                        color = Color.White.copy(alpha = a),
                        fontSize = (tile * 0.22f).toSp(),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            confetti.forEach { pc ->
                val a = pc.life.coerceIn(0f, 1f)
                val px = pc.x * boardPx
                val py = pc.y * boardPx
                val w = tile * 0.09f
                val h = tile * 0.055f
                rotate(pc.rot, Offset(px, py)) {
                    drawRect(
                        color = pc.color.copy(alpha = a),
                        topLeft = Offset(px - w / 2f, py - h / 2f),
                        size = Size(w, h)
                    )
                }
            }
        }
    }
}
