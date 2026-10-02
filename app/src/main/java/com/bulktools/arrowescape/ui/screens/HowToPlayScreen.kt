package com.bulktools.arrowescape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bulktools.arrowescape.R
import com.bulktools.arrowescape.theme.LocalThemeDef

@Composable
fun HowToPlayScreen(navController: NavController) {
    val def = LocalThemeDef.current
    val paragraphs = listOf(
        R.string.howto_p1,
        R.string.howto_p2,
        R.string.howto_p3,
        R.string.howto_p4
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(def.bgTop, def.bgBottom)))
    ) {
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
                text = stringResource(id = R.string.howto_title),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            paragraphs.forEach { res ->
                Row(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = "•",
                        color = def.accent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = res),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
