package com.blushelf.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.blushelf.app.R
import com.blushelf.app.ShelfKind
import com.blushelf.app.data.MediaItem
import com.blushelf.app.data.MediaKind
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class SwipeAction { SKIP, SHORTLIST, PICK }
private data class SwipeHistory(val index: Int, val itemId: String, val action: SwipeAction)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(allItems: List<MediaItem>, shelfKind: ShelfKind, onGoShelf: () -> Unit) {
    val candidates = allItems.filter { it.kind.name == shelfKind.name }
    var index by rememberSaveable(shelfKind) { mutableIntStateOf(0) }
    var history by remember { mutableStateOf<SwipeHistory?>(null) }
    val shortlist = remember(shelfKind) { mutableStateListOf<String>() }
    var showShortlist by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf<MediaItem?>(null) }
    val current = if (candidates.isEmpty()) null else candidates[index % candidates.size]

    fun act(action: SwipeAction) {
        val item = current ?: return
        history = SwipeHistory(index, item.id, action)
        if (action == SwipeAction.SHORTLIST && item.id !in shortlist) shortlist += item.id
        if (action == SwipeAction.PICK) picked = item else index++
    }

    Scaffold(topBar = { TopAppBar(title = { Column { Text(stringResource(R.string.swipe)); Text(if (shelfKind == ShelfKind.VIDEO) stringResource(R.string.video_shelf) else stringResource(R.string.audio_shelf), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }, actions = { IconButton({ showShortlist = true }) { Icon(Icons.Outlined.Bookmarks, stringResource(R.string.shortlist)) }; Text(shortlist.size.toString(), modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.primary) }) }) { padding ->
        if (current == null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Shuffle, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.swipe_empty), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
                Text(stringResource(R.string.swipe_empty_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                Button(onGoShelf) { Text(stringResource(R.string.go_to_shelf)) }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { AssistChip({}, { Text(stringResource(R.string.random_mode)) }, leadingIcon = { Icon(Icons.Outlined.Shuffle, null) }); AssistChip({}, { Text(stringResource(R.string.all_media)) }, leadingIcon = { Icon(Icons.Outlined.FilterList, null) }) }
                SwipeCard(current, { act(it) }, Modifier.weight(1f))
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton({ act(SwipeAction.SKIP) }) { Icon(Icons.Outlined.ArrowBack, null); Text(stringResource(R.string.skip_media), Modifier.padding(start = 6.dp)) }
                    FilledTonalButton({ act(SwipeAction.PICK) }) { Icon(Icons.Outlined.Check, null); Text(stringResource(R.string.pick), Modifier.padding(start = 6.dp)) }
                    Button({ act(SwipeAction.SHORTLIST) }) { Text(stringResource(R.string.shortlist), Modifier.padding(end = 6.dp)); Icon(Icons.Outlined.ArrowForward, null) }
                }
                TextButton(onClick = {
                    history?.let { last -> index = last.index; if (last.action == SwipeAction.SHORTLIST) shortlist.remove(last.itemId); history = null }
                }, enabled = history != null) { Icon(Icons.Outlined.Undo, null); Text(stringResource(R.string.undo), Modifier.padding(start = 6.dp)) }
            }
        }
    }

    if (showShortlist) ShortlistDialog(candidates.filter { it.id in shortlist }, { showShortlist = false }, { id -> shortlist.remove(id) })
    picked?.let { item -> AlertDialog(onDismissRequest = { picked = null }, icon = { Icon(Icons.Outlined.Star, null) }, title = { Text(stringResource(R.string.your_pick)) }, text = { Text("${item.title}\n${listOfNotNull(item.year?.toString(), item.format).joinToString(" · ")}") }, confirmButton = { Button({ picked = null }) { Text(stringResource(R.string.continue_swiping)) } }, dismissButton = { TextButton({ picked = null }) { Text(stringResource(R.string.details)) } }) }
}

@Composable
private fun SwipeCard(item: MediaItem, onAction: (SwipeAction) -> Unit, modifier: Modifier = Modifier) {
    var x by remember(item.id) { mutableFloatStateOf(0f) }
    var y by remember(item.id) { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(x / 40f, label = "card rotation")
    Card(modifier.fillMaxWidth().widthIn(max = 440.dp).padding(vertical = 8.dp).offset { IntOffset(x.roundToInt(), y.roundToInt()) }.graphicsLayer { rotationZ = rotation }.pointerInput(item.id) {
        detectDragGestures(onDragEnd = {
            when { abs(x) > 180 -> onAction(if (x > 0) SwipeAction.SHORTLIST else SwipeAction.SKIP); y < -160 -> onAction(SwipeAction.PICK) }
            x = 0f; y = 0f
        }) { _, drag -> x += drag.x; y += drag.y }
    }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(coverColor(item)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(if (item.kind == MediaKind.VIDEO) Icons.Outlined.Replay else Icons.Outlined.Shuffle, null, Modifier.size(64.dp), tint = Color.White); Text(stringResource(R.string.artwork_placeholder), color = Color.White, style = MaterialTheme.typography.labelLarge) }
            }
            Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            Text(listOfNotNull(item.year?.toString(), item.format).joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!item.played) AssistChip({}, { Text(stringResource(if (item.kind == MediaKind.VIDEO) R.string.unwatched else R.string.unheard)) }); item.rating?.let { AssistChip({}, { Text("★ %.1f".format(it)) }) }; if (item.favorite) AssistChip({}, { Text(stringResource(R.string.favorite)) }) }
            Text(stringResource(R.string.swipe_gesture_hint), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

private fun coverColor(item: MediaItem) = when {
    item.format.contains("UHD", true) -> Color(0xFF263238)
    item.format.contains("Blu", true) -> Color(0xFF0D47A1)
    item.format.contains("Vinyl", true) -> Color(0xFF00695C)
    item.format.contains("CD", true) -> Color(0xFF8E244D)
    else -> Color(0xFF5E548E)
}

@Composable
private fun ShortlistDialog(items: List<MediaItem>, onDismiss: () -> Unit, onRemove: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.session_shortlist)) }, text = { if (items.isEmpty()) Text(stringResource(R.string.shortlist_empty)) else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { items.take(10).forEach { item -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(item.title, fontWeight = FontWeight.SemiBold); Text(item.format, style = MaterialTheme.typography.bodySmall) }; IconButton({ onRemove(item.id) }) { Icon(Icons.Outlined.Undo, stringResource(R.string.remove)) } } } } }, confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.close)) } })
}
