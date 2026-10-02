package com.bulktools.arrowescape.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bulktools.arrowescape.R
import com.bulktools.arrowescape.audio.SoundManager
import com.bulktools.arrowescape.data.GameRepository
import com.bulktools.arrowescape.theme.AppThemes
import com.bulktools.arrowescape.theme.LocalThemeDef
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { GameRepository.getInstance(context) }
    val soundManager = remember { SoundManager.getInstance(context) }
    val def = LocalThemeDef.current
    val scope = rememberCoroutineScope()
    val soundOn by repo.soundEnabled.collectAsState(initial = true)
    val hapticsOn by repo.hapticsEnabled.collectAsState(initial = true)
    val themeIndex by repo.themeIndex.collectAsState(initial = 0)
    var showResetConfirm by remember { mutableStateOf(false) }

    val themeNames = listOf(
        R.string.theme_midnight,
        R.string.theme_sunset,
        R.string.theme_forest,
        R.string.theme_candy
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(def.bgTop, def.bgBottom)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 20.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Text(
                    text = stringResource(id = R.string.nav_settings),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = def.tile)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.set_sound),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = soundOn,
                                onCheckedChange = {
                                    scope.launch {
                                        repo.setSoundEnabled(it)
                                        soundManager.enabled = it
                                    }
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.set_haptics),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = hapticsOn,
                                onCheckedChange = {
                                    scope.launch { repo.setHapticsEnabled(it) }
                                }
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = def.tile)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(id = R.string.set_theme),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemes.all.forEachIndexed { i, themeDef ->
                                val selected = i == themeIndex
                                val buttonColors = if (selected) {
                                    ButtonDefaults.buttonColors(containerColor = def.accent)
                                } else {
                                    ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                }
                                if (selected) {
                                    Button(
                                        onClick = { scope.launch { repo.setThemeIndex(i) } },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = buttonColors,
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 4.dp,
                                            vertical = 10.dp
                                        )
                                    ) {
                                        ThemeChipContent(themeDef.accent, stringResource(id = themeNames[i]))
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { scope.launch { repo.setThemeIndex(i) } },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = buttonColors,
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 4.dp,
                                            vertical = 10.dp
                                        )
                                    ) {
                                        ThemeChipContent(themeDef.accent, stringResource(id = themeNames[i]))
                                    }
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B).copy(alpha = 0.18f),
                        contentColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.set_reset),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(id = R.string.dlg_reset_title)) },
            text = { Text(stringResource(id = R.string.dlg_reset_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    scope.launch { repo.resetAll() }
                }) {
                    Text(
                        stringResource(id = R.string.btn_confirm),
                        color = Color(0xFFFF6B6B)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(id = R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun ThemeChipContent(dotColor: Color, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = name, fontSize = 11.sp, maxLines = 1)
    }
}
