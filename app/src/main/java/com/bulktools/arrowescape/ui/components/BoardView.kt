package com.bulktools.arrowescape.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    shakeOffsets: SnapshotStateMap<Cell, Float> = remember { androidx.compose.runtime.mutableStateMapOf() },
    modifier: Modifier = Modifier
) {
    val gridN = size
    val tapHandler by rememberUpdatedState(onCellTap)
    val textMeasurer = rememberTextMeasurer()
    var tick by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
            last = now
            (particles as? MutableList<Particle>)?.let { list ->
                val it = list.iterator()
                while (it.hasNext()) {
                    val p = it.next()
                    p.life -= dt * 1.4f
                    if (p.life <= 0f) {
                        it.remove()
                    } else {
                        p.ox += p.vx * dt
                        p.oy += p.vy * dt
                        p.vy += 2.5f * dt
                    }
                }
            }
            (popups as? MutableList<ScorePopup>)?.let { list ->
                val it = list.iterator()
                while (it.hasNext()) {
                    val p = it.next()
                    p.life -= dt * 0.8f
                    if (p.life <= 0f) it.remove()
                }
            }
            (confetti as? MutableList<ConfettiPiece>)?.let { list ->
                val it = list.iterator()
                while (it.hasNext()) {
                    val c = it.next()
                    c.y += c.vy * dt
                    c.rot += c.vrot * dt
                    c.life -= dt * 0.35f
                    if (c.y > 1.25f || c.life <= 0f) it.remove()
                }
            }
            if (particles.isNotEmpty() || popups.isNotEmpty() || confetti.isNotEmpty()) tick++
        }
    }

    val pulseAlpha by androidx.compose.animation.core.rememberInfiniteTransition(label = "hint")
        .animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(900),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "hintPulse"
        )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(gridN) {
                detectTapGestures { offset ->
                    val tile = size.width.toFloat() / gridN
                    if (tile <= 0f) return@detectTapGestures
                    val x = (offset.x / tile).toInt().coerceIn(0, gridN - 1)
                    val y = (offset.y / tile).toInt().coerceIn(0, gridN - 1)
                    tapHandler(Cell(x, y))
                }
            }
    ) {
        val frame = tick
        val tile = size.width / gridN
        if (tile <= 0f) return@Canvas
        val pad = 3.dp.toPx()
        val borderColor = Color.White.copy(alpha = 0.07f)

        for (gy in 0 until gridN) {
            for (gx in 0 until gridN) {
                val tl = Offset(gx * tile + pad, gy * tile + pad)
                val ts = Size(tile - pad * 2f, tile - pad * 2f)
                drawRoundRect(
                    color = def.tile,
                    topLeft = tl,
                    size = ts,
                    cornerRadius = CornerRadius(tile * 0.18f, tile * 0.18f)
                )
                drawRoundRect(
                    color = borderColor,
                    topLeft = tl,
                    size = ts,
                    cornerRadius = CornerRadius(tile * 0.18f, tile * 0.18f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )
            }
        }

        displayGrid.forEach { (cell, disp) ->
            val center = Offset((cell.x + 0.5f) * tile, (cell.y + 0.5f) * tile)
            val shake = shakeOffsets[cell] ?: 0f
            val sx = if (shake > 0f) sin(shake * 4f * PI.toFloat()) * shake * tile * 0.12f else 0f
            if (shake > 0f) {
                drawCircle(
                    color = Color.Red.copy(alpha = 0.35f * shake),
                    radius = tile * 0.42f,
                    center = center
                )
            }
            val c = center + Offset(sx, 0f)
            if (disp.removing) {
                val p = disp.removeProgress.coerceIn(0f, 1f)
                val d = disp.arrow.direction
                val off = Offset(d.dx * p * tile * 0.9f, d.dy * p * tile * 0.9f)
                withTransform({ translate(off.x, off.y) }) {
                    drawArrowRaw(this, disp.arrow, def, c, tile * 0.66f, 1f - p)
                }
            } else {
                if (disp.hintPulse) {
                    drawCircle(
                        color = def.gold.copy(alpha = 0.55f * pulseAlpha),
                        radius = tile * 0.46f,
                        center = c,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx())
                    )
                }
                drawArrowRaw(this, disp.arrow, def, c, tile * 0.66f, 1f)
            }
        }

        particles.forEach { p ->
            val px = (p.cell.x + p.ox) * tile
            val py = (p.cell.y + p.oy) * tile
            val a = p.life.coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = a),
                radius = (2.5f + 3f * a).dp.toPx(),
                center = Offset(px, py)
            )
        }

        popups.forEach { p ->
            val a = p.life.coerceIn(0f, 1f)
            val cx = (p.cell.x + 0.5f) * tile
            val cy = (p.cell.y + 0.5f) * tile - (1f - p.life) * tile * 0.8f
            drawText(
                textMeasurer = textMeasurer,
                text = p.text,
                topLeft = Offset(cx - 30.dp.toPx(), cy - 12.dp.toPx()),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                alpha = a
            )
        }

        val boardW = size.width
        confetti.forEach { c ->
            val px = c.x * boardW
            val py = c.y * boardW
            val a = c.life.coerceIn(0f, 1f)
            val w = 10.dp.toPx()
            val h = 6.dp.toPx()
            rotate(c.rot, Offset(px, py)) {
                drawRect(
                    color = c.color.copy(alpha = a),
                    topLeft = Offset(px - w / 2f, py - h / 2f),
                    size = Size(w, h)
                )
            }
        }
        @Suppress("UNUSED_EXPRESSION")
        frame
    }
}
