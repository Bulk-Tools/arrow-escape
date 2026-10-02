package com.bulktools.arrowescape.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Haptic feedback helpers, gated by the user's haptics setting.
 */
object Haptics {
    @Composable
    fun current(): HapticFeedback = LocalHapticFeedback.current
}

/** Standard tap feedback. */
fun HapticFeedback.tap(enabled: Boolean) {
    if (enabled) performHapticFeedback(HapticFeedbackType.LongPress)
}

/** Error feedback. */
fun HapticFeedback.error(enabled: Boolean) {
    if (enabled) performHapticFeedback(HapticFeedbackType.TextHandleMove)
}

/** Success feedback. */
fun HapticFeedback.success(enabled: Boolean) {
    if (enabled) performHapticFeedback(HapticFeedbackType.LongPress)
}
