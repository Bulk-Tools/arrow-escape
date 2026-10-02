package com.bulktools.arrowescape.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bulktools.arrowescape.R

@Composable
fun GameTopBar(
    title: String,
    score: Int,
    lives: Int?,
    timeLeft: Int?,
    hintsLeft: Int,
    canUndo: Boolean,
    onHint: () -> Unit,
    onUndo: () -> Unit,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(id = R.string.hud_score),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        if (lives != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = lives.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
        if (timeLeft != null) {
            Text(
                text = timeLeft.toString(),
                color = if (timeLeft <= 10) Color(0xFFFF6B6B) else Color.White,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        Box {
            IconButton(onClick = onHint) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD166)
                )
            }
            Text(
                text = hintsLeft.toString(),
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 4.dp)
            )
        }
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                imageVector = Icons.Filled.Undo,
                contentDescription = null,
                tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f)
            )
        }
        IconButton(onClick = onPause) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
