package dev.anilbeesetti.nextplayer.feature.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.feature.player.extensions.formatted
import dev.anilbeesetti.nextplayer.feature.player.state.MediaPresentationState
import dev.anilbeesetti.nextplayer.feature.player.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
fun BoxScope.MarkIntroView(
    modifier: Modifier = Modifier,
    show: Boolean,
    player: Player,
    mediaPresentationState: MediaPresentationState,
    onDismiss: () -> Unit,
    viewModel: PlayerViewModel,
    folderPath: String?,
) {
    var introStartMs by remember { mutableStateOf(0L) }
    var introEndMs by remember { mutableStateOf(0L) }
    var activeField by remember { mutableStateOf<ActiveField?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(folderPath) {
        if (folderPath != null && !loaded) {
            val timestamps = viewModel.getFolderIntroTimestamps(folderPath)
            if (timestamps != null) {
                introStartMs = timestamps.first
                introEndMs = timestamps.second
            } else {
                introStartMs = mediaPresentationState.introStartMs
                introEndMs = mediaPresentationState.introEndMs
            }
            loaded = true
        }
    }

    OverlayView(
        modifier = modifier,
        show = show,
        title = stringResource(R.string.mark_intro),
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.current_position),
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = mediaPresentationState.position.milliseconds.formatted(),
                    style = TextStyle(fontSize = 16.sp)
                )
            }
            Spacer(modifier = Modifier.size(24.dp))

            IntroTimeInput(
                title = stringResource(R.string.intro_start),
                value = introStartMs,
                onValueChange = { introStartMs = it },
                onSetCurrent = { introStartMs = mediaPresentationState.position },
                isActive = activeField == ActiveField.START,
                onActivate = { activeField = ActiveField.START },
            )
            Spacer(modifier = Modifier.size(16.dp))
            IntroTimeInput(
                title = stringResource(R.string.intro_end),
                value = introEndMs,
                onValueChange = { introEndMs = it },
                onSetCurrent = { introEndMs = mediaPresentationState.position },
                isActive = activeField == ActiveField.END,
                onActivate = { activeField = ActiveField.END },
            )
            Spacer(modifier = Modifier.size(24.dp))
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (introStartMs >= 0 && introEndMs > introStartMs && folderPath != null) {
                        viewModel.updateFolderIntroTimestamps(folderPath, introStartMs, introEndMs)
                        onDismiss()
                    }
                },
                enabled = introStartMs >= 0 && introEndMs > introStartMs && folderPath != null,
            ) {
                Text(text = stringResource(R.string.save))
            }
            Spacer(modifier = Modifier.size(16.dp))
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (folderPath != null) {
                        viewModel.clearFolderIntroTimestamps(folderPath)
                    }
                    introStartMs = 0
                    introEndMs = 0
                    onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.clear_intro))
            }
        }
    }
}

enum class ActiveField { START, END }

@Composable
private fun IntroTimeInput(
    title: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    onSetCurrent: () -> Unit,
    isActive: Boolean,
    onActivate: () -> Unit,
) {
    var valueString by remember {
        mutableStateOf(value.milliseconds.formatted())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 16.sp)
        )
        OutlinedTextField(
            value = valueString,
            onValueChange = { newValue: String ->
                valueString = newValue
                val ms = parseTimeString(newValue)
                if (ms != null) onValueChange(ms)
            },
            label = { Text(text = "HH:MM:SS") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        FilledTonalButton(onClick = {
            onSetCurrent()
        }) {
            Text(text = stringResource(R.string.use_current))
        }
    }
}

private fun parseTimeString(input: String): Long? {
    return try {
        val parts = input.split(":").map { it.toLongOrNull() }.filterNotNull()
        when (parts.size) {
            3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
            2 -> (parts[0] * 60 + parts[1]) * 1000
            1 -> parts[0] * 1000
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}