package com.bulktools.arrowescape.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bulktools.arrowescape.R
import com.bulktools.arrowescape.Routes
import com.bulktools.arrowescape.audio.SoundManager
import com.bulktools.arrowescape.data.GameRepository
import com.bulktools.arrowescape.engine.BoardState
import com.bulktools.arrowescape.engine.Cell
import com.bulktools.arrowescape.engine.LevelGenerator
import com.bulktools.arrowescape.engine.LevelSpec
import com.bulktools.arrowescape.engine.Scoring
import com.bulktools.arrowescape.engine.Special
import com.bulktools.arrowescape.engine.TapResult
import com.bulktools.arrowescape.engine.Worlds
import com.bulktools.arrowescape.theme.LocalThemeDef
import com.bulktools.arrowescape.ui.components.BoardView
import com.bulktools.arrowescape.ui.components.ConfettiPiece
import com.bulktools.arrowescape.ui.components.DispArrow
import com.bulktools.arrowescape.ui.components.GameTopBar
import com.bulktools.arrowescape.ui.components.Particle
import com.bulktools.arrowescape.ui.components.ScorePopup
import com.bulktools.arrowescape.util.Haptics
import com.bulktools.arrowescape.util.error
import com.bulktools.arrowescape.util.success
import com.bulktools.arrowescape.util.tap
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun GameScreen(navController: NavController, mode: String, world: Int, level: Int) {
    val context = LocalContext.current
    val repo = remember { GameRepository.getInstance(context) }
    val soundManager = remember { SoundManager.getInstance(context) }
    val haptics = Haptics.current()
    val def = LocalThemeDef.current
    val scope = rememberCoroutineScope()
    var restartKey by remember { mutableStateOf(0) }

    key(restartKey) {
        val soundOn by repo.soundEnabled.collectAsState(initial = true)
        val hapticsOn by repo.hapticsEnabled.collectAsState(initial = true)
        val bestComboFlow by repo.bestCombo.collectAsState(initial = 0)

        val spec: LevelSpec? = remember(mode, world, level) {
            if (mode == "classic") Worlds.spec(world, level) else null
        }
        val today = remember { LocalDate.now() }
        val todayStr = remember { today.format(DateTimeFormatter.BASIC_ISO_DATE) }

        val boardSetup = remember(mode, world, level) {
            when (mode) {
                "classic" -> {
                    val s = Worlds.spec(world, level)
                    s.size to LevelGenerator.generate(s)
                }
                "daily" -> {
                    val seed = todayStr.toLong()
                    7 to LevelGenerator.generate(7, 0.6, 0.06, 0.05, 0.05, Random(seed))
                }
                else -> {
                    6 to LevelGenerator.generate(6, 0.55, 0.05, 0.04, 0.0, Random(System.currentTimeMillis()))
                }
            }
        }
        var boardState by remember(mode, world, level) {
            mutableStateOf(BoardState(boardSetup.first, boardSetup.second))
        }
        val displayGrid = remember(mode, world, level) {
            mutableStateMapOf<Cell, DispArrow>().apply {
                boardState.allCells().forEach { (c, a) -> put(c, DispArrow(a)) }
            }
        }

        var score by remember { mutableStateOf(0) }
        var combo by remember { mutableStateOf(0) }
        var lives by remember { mutableStateOf(if (mode == "classic" || mode == "daily") 3 else Int.MAX_VALUE) }
        var timeLeft by remember { mutableStateOf(60) }
        var hintsLeft by remember { mutableStateOf(3) }
        val undoScores = remember { ArrayDeque<Int>() }
        var paused by remember { mutableStateOf(false) }
        var showWin by remember { mutableStateOf(false) }
        var showLose by remember { mutableStateOf(false) }
        var showTimesUp by remember { mutableStateOf(false) }
        var winTitleRes by remember { mutableStateOf(R.string.dlg_you_win) }
        var winStars by remember { mutableStateOf(0) }
        var winBonus by remember { mutableStateOf(0) }
        var hintCell by remember { mutableStateOf<Cell?>(null) }
        val shakeOffsets = remember { mutableStateMapOf<Cell, Float>() }
        val particles = remember { mutableStateListOf<Particle>() }
        val popups = remember { mutableStateListOf<ScorePopup>() }
        val confetti = remember { mutableStateListOf<ConfettiPiece>() }
        val snackbarHostState = remember { SnackbarHostState() }

        val noHintsMsg = context.getString(R.string.msg_no_hints)

        fun spawnConfetti() {
            val colors = listOf(def.gold, def.accent, Color.White, def.arrow)
            repeat(90) {
                confetti.add(
                    ConfettiPiece(
                        x = Random.nextFloat(),
                        y = -Random.nextFloat() * 0.3f,
                        vy = 0.35f + Random.nextFloat() * 0.5f,
                        rot = Random.nextFloat() * 360f,
                        vrot = (Random.nextFloat() - 0.5f) * 540f,
                        life = 1f,
                        color = colors.random()
                    )
                )
            }
        }

        fun onBoardCleared() {
            when (mode) {
                "classic" -> {
                    val bonus = Scoring.winBonus(lives)
                    score += bonus
                    val stars = Scoring.starsFor(lives)
                    winStars = stars
                    winBonus = bonus
                    winTitleRes = R.string.dlg_you_win
                    if (soundOn) soundManager.play("win")
                    haptics.success(hapticsOn)
                    spawnConfetti()
                    scope.launch {
                        repo.saveStars(world, level, stars)
                        repo.setBestCombo(maxOf(bestComboFlow, combo))
                        repo.incrementGamesPlayed()
                    }
                    showWin = true
                }
                "daily" -> {
                    winStars = 0
                    winBonus = 0
                    winTitleRes = R.string.dlg_daily_done
                    if (soundOn) soundManager.play("win")
                    haptics.success(hapticsOn)
                    spawnConfetti()
                    scope.launch {
                        val last = repo.lastDailyDate.first()
                        val yesterdayStr = today.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)
                        if (last != todayStr) {
                            val current = repo.dailyStreak.first()
                            val newStreak = if (last == yesterdayStr) current + 1 else 1
                            repo.setDailyStreak(newStreak)
                            repo.setLastDailyDate(todayStr)
                        }
                        repo.incrementGamesPlayed()
                    }
                    showWin = true
                }
                else -> {
                    if (soundOn) soundManager.play("win")
                    val fresh = LevelGenerator.generate(
                        boardSetup.first, 0.55, 0.05, 0.04, 0.0, Random(System.currentTimeMillis())
                    )
                    boardState = BoardState(boardSetup.first, fresh)
                    displayGrid.clear()
                    fresh.forEach { (c, a) -> displayGrid[c] = DispArrow(a) }
                    hintCell = null
                }
            }
        }

        fun tap(cell: Cell) {
            if (paused || showWin || showLose || showTimesUp) return
            val disp = displayGrid[cell] ?: return
            if (disp.removing) return
            if (cell == hintCell) {
                displayGrid[cell] = disp.copy(hintPulse = false)
                hintCell = null
            }
            val useCombo = (combo + 1).coerceAtMost(Scoring.MAX_COMBO)
            val wasGolden = disp.arrow.special == Special.GOLDEN
            when (val r = boardState.tap(cell, useCombo)) {
                is TapResult.Removed -> {
                    combo = useCombo
                    score += r.gained
                    undoScores.addLast(r.gained)
                    if (soundOn) soundManager.play(if (wasGolden) "star" else "pop")
                    haptics.tap(hapticsOn)
                    r.cells.forEach { c ->
                        displayGrid[c]?.let { displayGrid[c] = it.copy(removing = true) }
                    }
                    r.cells.forEach { c ->
                        val col = if (wasGolden && c == cell) def.gold else def.accent
                        repeat(6) {
                            particles.add(
                                Particle(
                                    cell = c,
                                    ox = 0.5f,
                                    oy = 0.5f,
                                    vx = Random.nextFloat() * 2f - 1f,
                                    vy = Random.nextFloat() * 2f - 1f,
                                    life = 1f,
                                    color = col
                                )
                            )
                        }
                    }
                    popups.add(ScorePopup(cell = cell, text = "+${r.gained}", life = 1f))
                    scope.launch {
                        val anim = Animatable(0f)
                        anim.animateTo(1f, tween(350)) {
                            val v = value
                            r.cells.forEach { c ->
                                displayGrid[c]?.let { displayGrid[c] = it.copy(removeProgress = v) }
                            }
                        }
                        r.cells.forEach { c -> displayGrid.remove(c) }
                    }
                    scope.launch { repo.addCleared(r.cells.size) }
                    if (boardState.isCleared()) onBoardCleared()
                }
                is TapResult.Unfrozen -> {
                    displayGrid[cell] = disp.copy(arrow = disp.arrow.copy(frozen = false))
                    if (soundOn) soundManager.play("unfreeze")
                    haptics.tap(hapticsOn)
                }
                is TapResult.Blocked -> {
                    combo = 0
                    if (soundOn) soundManager.play("error")
                    haptics.error(hapticsOn)
                    displayGrid[cell] = disp.copy(shakeKey = disp.shakeKey + 1)
                    scope.launch {
                        val anim = Animatable(1f)
                        anim.animateTo(0f, tween(450)) { shakeOffsets[cell] = value }
                        shakeOffsets.remove(cell)
                    }
                    if (mode == "classic" || mode == "daily") {
                        lives--
                        if (lives <= 0) showLose = true
                    } else if (mode == "blitz") {
                        timeLeft = max(0, timeLeft - 2)
                    }
                }
            }
        }

        fun doHint() {
            if (paused || showWin || showLose || showTimesUp) return
            if (hintsLeft <= 0) {
                if (soundOn) soundManager.play("error")
                scope.launch { snackbarHostState.showSnackbar(noHintsMsg) }
                return
            }
            val target = boardState.allCells()
                .firstOrNull { (c, _) -> boardState.isFree(c) && displayGrid[c]?.let { !it.removing } == true }
                ?.first
            if (target != null) {
                hintCell?.let { hc ->
                    displayGrid[hc]?.let { displayGrid[hc] = it.copy(hintPulse = false) }
                }
                hintCell = target
                displayGrid[target]?.let { displayGrid[target] = it.copy(hintPulse = true) }
                hintsLeft--
                if (soundOn) soundManager.play("click")
            }
        }

        fun doUndo() {
            if (paused || showWin || showLose || showTimesUp) return
            if (boardState.undo()) {
                val g = if (undoScores.isNotEmpty()) undoScores.removeLast() else 0
                score = (score - g).coerceAtLeast(0)
                hintCell = null
                displayGrid.clear()
                boardState.allCells().forEach { (c, a) -> displayGrid[c] = DispArrow(a) }
                if (soundOn) soundManager.play("click")
            }
        }

        LaunchedEffect(mode) {
            if (mode == "blitz") {
                while (true) {
                    delay(1000)
                    if (!paused && !showWin && !showLose && !showTimesUp) {
                        if (timeLeft > 0) {
                            timeLeft--
                        } else {
                            showTimesUp = true
                            break
                        }
                    }
                }
            }
        }

        val worldName = Worlds.all.getOrNull(world)?.name ?: ""
        val title = when (mode) {
            "classic" -> "$worldName · ${level + 1}"
            "blitz" -> context.getString(R.string.mode_blitz)
            "zen" -> context.getString(R.string.mode_zen)
            "daily" -> context.getString(R.string.mode_daily)
            else -> ""
        }

        val tutorialRes = spec?.tutorialIndex?.let {
            when (it) {
                0 -> R.string.msg_tutorial_1
                1 -> R.string.msg_tutorial_2
                2 -> R.string.msg_tutorial_3
                else -> null
            }
        }
        val bottomText = when {
            tutorialRes != null -> stringResource(id = tutorialRes)
            combo >= 2 -> "${stringResource(id = R.string.combo_label)} $combo"
            else -> ""
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(def.bgTop, def.bgBottom)))
                    .padding(padding)
            ) {
                GameTopBar(
                    title = title,
                    score = score,
                    lives = if (mode == "classic" || mode == "daily") lives else null,
                    timeLeft = if (mode == "blitz") timeLeft else null,
                    hintsLeft = hintsLeft,
                    canUndo = undoScores.isNotEmpty(),
                    onHint = ::doHint,
                    onUndo = ::doUndo,
                    onPause = { paused = true }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BoardView(
                        size = boardSetup.first,
                        displayGrid = displayGrid,
                        def = def,
                        onCellTap = ::tap,
                        particles = particles,
                        popups = popups,
                        confetti = confetti,
                        shakeOffsets = shakeOffsets,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Text(
                    text = bottomText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    minLines = 1
                )
            }
        }

        if (paused) {
            AlertDialog(
                onDismissRequest = { paused = false },
                title = { Text(stringResource(id = R.string.dlg_paused)) },
                confirmButton = {
                    TextButton(onClick = { paused = false }) {
                        Text(stringResource(id = R.string.btn_resume))
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { paused = false; restartKey++ }) {
                            Text(stringResource(id = R.string.btn_restart))
                        }
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(stringResource(id = R.string.btn_quit))
                        }
                    }
                }
            )
        }

        if (showWin) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = stringResource(id = winTitleRes),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (mode == "classic") {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(3) { i ->
                                    val target = if (winStars > i) 1f else 0.4f
                                    val scale by animateFloatAsState(
                                        targetValue = target,
                                        animationSpec = tween(300, delayMillis = i * 150),
                                        label = "winStar$i"
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (winStars > i) def.gold else Color.Gray,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .scale(scale)
                                            .padding(4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = "${stringResource(id = R.string.hud_score)}: $score",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        if (mode == "classic" && winBonus > 0) {
                            Text(
                                text = "+$winBonus",
                                fontSize = 16.sp,
                                color = def.gold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (mode == "classic") {
                            if (level < Worlds.LEVELS_PER_WORLD - 1) {
                                TextButton(onClick = {
                                    navController.popBackStack()
                                    navController.navigate(Routes.game("classic", world, level + 1))
                                }) {
                                    Text(stringResource(id = R.string.btn_next))
                                }
                            }
                            TextButton(onClick = {
                                navController.popBackStack()
                                navController.navigate(Routes.levelMap(world))
                            }) {
                                Text(stringResource(id = R.string.btn_level_map))
                            }
                        } else {
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text(stringResource(id = R.string.btn_close))
                            }
                        }
                        TextButton(onClick = { showWin = false; restartKey++ }) {
                            Text(stringResource(id = R.string.btn_replay))
                        }
                    }
                }
            )
        }

        if (showLose) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = stringResource(id = R.string.dlg_you_lose),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "${stringResource(id = R.string.hud_score)}: $score",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showLose = false; restartKey++ }) {
                            Text(stringResource(id = R.string.btn_retry))
                        }
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(stringResource(id = R.string.btn_quit))
                        }
                    }
                }
            )
        }

        if (showTimesUp) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = stringResource(id = R.string.dlg_times_up),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "${stringResource(id = R.string.hud_score)}: $score",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimesUp = false; restartKey++ }) {
                            Text(stringResource(id = R.string.btn_retry))
                        }
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(stringResource(id = R.string.btn_quit))
                        }
                    }
                }
            )
        }
    }
}
