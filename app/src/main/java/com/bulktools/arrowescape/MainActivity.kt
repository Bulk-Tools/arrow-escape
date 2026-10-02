package com.bulktools.arrowescape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bulktools.arrowescape.audio.SoundManager
import com.bulktools.arrowescape.data.GameRepository
import com.bulktools.arrowescape.theme.GameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val repo = remember { GameRepository.getInstance(context) }
            val soundManager = remember { SoundManager.getInstance(context) }
            val themeIndex by repo.themeIndex.collectAsState(initial = 0)
            val soundEnabled by repo.soundEnabled.collectAsState(initial = true)
            LaunchedEffect(soundEnabled) { soundManager.enabled = soundEnabled }
            DisposableEffect(Unit) { onDispose { soundManager.release() } }
            GameTheme(themeIndex = themeIndex) { GameApp() }
        }
    }
}
