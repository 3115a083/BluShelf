package com.blushelf.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blushelf.app.R
import com.blushelf.app.ShelfKind
import com.blushelf.app.data.MediaItem
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

private enum class SwipeAction { SKIP, SHORTLIST, PICK }
private data class SwipeHistory(val index: Int, val itemId: String, val action: SwipeAction)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(allItems: List<MediaItem>, shelfKind: ShelfKind, shelfId: String, onGoShelf: () -> Unit) {
    var shuffled by rememberSaveable(shelfId) { mutableStateOf(false) }
    var onlyUnplayed by rememberSaveable(shelfId) { mutableStateOf(false) }
    val candidates = remember(allItems, shelfKind, shelfId, shuffled, onlyUnplayed) {
        val filtered = allItems.filter { it.shelfId == shelfId && it.kind.name == shelfKind.name && (!onlyUnplayed || !it.played) }
        if (shuffled) filtered.shuffled(Random(filtered.joinToString { it.id }.hashCode())) else filtered
    }
    var index by rememberSaveable(shelfId, shuffled, onlyUnplayed) { mutableIntStateOf(0) }
    var history by remember { mutableStateOf<SwipeHistory?>(null) }
    val shortlist = remember(shelfId) { mutableStateListOf<String>() }
    var showShortlist by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf<MediaItem?>(null) }
    val current = candidates.getOrNull(index)

    fun act(action: SwipeAction) {
        val item = current ?: return
        history = SwipeHistory(index, item.id, action)
        if (action == SwipeAction.SHORTLIST && item.id !in shortlist) shortlist += item.id
        if (action == SwipeAction.PICK) picked = item
        index += 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.swipe), fontWeight = FontWeight.Bold)
                        Text(
                            if (shelfKind == ShelfKind.VIDEO) stringResource(R.string.video_shelf) else stringResource(R.string.audio_shelf),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { showShortlist = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.Bookmarks, null, Modifier.size(20.dp))
                            Text(shortlist.size.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }
            )
        }
    ) { padding ->
        if (current == null) {
            SwipeEmpty(
                hasCollection = allItems.any { it.shelfId == shelfId && it.kind.name == shelfKind.name },
                onReset = { index = 0; onlyUnplayed = false },
                onGoShelf = onGoShelf,
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { shuffled = !shuffled },
                        label = { Text(stringResource(if (shuffled) R.string.random_on else R.string.random_mode), maxLines = 1) },
                        leadingIcon = { Icon(Icons.Outlined.Shuffle, null, Modifier.size(18.dp)) }
                    )
                    AssistChip(
                        onClick = { onlyUnplayed = !onlyUnplayed },
                        label = { Text(stringResource(if (onlyUnplayed) R.string.unplayed_only else R.string.all_media), maxLines = 1) },
                        leadingIcon = { Icon(Icons.Outlined.FilterList, null, Modifier.size(18.dp)) }
                    )
                }
                SwipeDeck(current, candidates.getOrNull(index + 1), { act(it) }, Modifier.weight(1f))
                SwipeControls(
                    canUndo = history != null,
                    onSkip = { act(SwipeAction.SKIP) },
                    onPick = { act(SwipeAction.PICK) },
                    onShortlist = { act(SwipeAction.SHORTLIST) },
                    onUndo = {
                        history?.let { last ->
                            index = last.index
                            if (last.action == SwipeAction.SHORTLIST) shortlist.remove(last.itemId)
                            history = null
                        }
                    }
                )
            }
        }
    }

    if (showShortlist) {
        ShortlistDialog(
            allItems.filter { it.id in shortlist },
            onDismiss = { showShortlist = false },
            onRemove = { shortlist.remove(it) }
        )
    }
    picked?.let { item ->
        AlertDialog(
            onDismissRequest = { picked = null },
            icon = { Icon(Icons.Outlined.Check, null) },
            title = { Text(stringResource(R.string.your_pick)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MediaCover(item, Modifier.width(160.dp).aspectRatio(.72f))
                    Text(item.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp), textAlign = TextAlign.Center)
                    Text(listOfNotNull(item.year?.toString(), item.format).joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { Button({ picked = null }) { Text(stringResource(R.string.continue_swiping)) } }
        )
    }
}

@Composable
private fun SwipeEmpty(hasCollection: Boolean, onReset: () -> Unit, onGoShelf: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Outlined.Shuffle, null, Modifier.padding(20.dp).size(42.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(
            stringResource(if (hasCollection) R.string.swipe_finished else R.string.swipe_empty),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 18.dp),
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(if (hasCollection) R.string.swipe_finished_hint else R.string.swipe_empty_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = TextAlign.Center
        )
        Button(if (hasCollection) onReset else onGoShelf) {
            Text(stringResource(if (hasCollection) R.string.start_again else R.string.go_to_shelf))
        }
    }
}

@Composable
private fun SwipeDeck(
    item: MediaItem,
    next: MediaItem?,
    onAction: (SwipeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxWidth().widthIn(max = 440.dp), contentAlignment = Alignment.Center) {
        next?.let {
            Card(
                Modifier.fillMaxWidth(.92f).fillMaxHeight(.94f).offset(y = 14.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                MediaCover(it, Modifier.fillMaxSize())
            }
        }
        SwipePosterCard(item, onAction, Modifier.fillMaxWidth().fillMaxHeight(.96f))
    }
}

@Composable
private fun SwipePosterCard(item: MediaItem, onAction: (SwipeAction) -> Unit, modifier: Modifier = Modifier) {
    val statusText = if (item.notes.isBlank()) stringResource(if (item.played) R.string.already_played else R.string.ready_to_choose) else item.notes
    var x by remember(item.id) { mutableFloatStateOf(0f) }
    var y by remember(item.id) { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState((x / 32f).coerceIn(-12f, 12f), label = "card rotation")
    Card(
        modifier
            .shadow(18.dp, RoundedCornerShape(28.dp))
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .graphicsLayer { rotationZ = rotation }
            .pointerInput(item.id) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            y < -150f && abs(y) > abs(x) -> onAction(SwipeAction.PICK)
                            x > 170f -> onAction(SwipeAction.SHORTLIST)
                            x < -170f -> onAction(SwipeAction.SKIP)
                        }
                        x = 0f
                        y = 0f
                    }
                ) { change, drag ->
                    change.consume()
                    x += drag.x
                    y += drag.y
                }
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box(Modifier.fillMaxSize()) {
            MediaCover(item, Modifier.fillMaxSize(), showLabels = false)
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = .88f)),
                        startY = 200f
                    )
                )
            )
            SwipeStamp(
                text = stringResource(R.string.shortlist),
                visible = x > 24f,
                color = Color(0xFF63D8A4),
                modifier = Modifier.align(Alignment.TopStart).padding(24.dp).graphicsLayer { rotationZ = -10f }
            )
            SwipeStamp(
                text = stringResource(R.string.skip_media),
                visible = x < -24f,
                color = Color(0xFFFF8A80),
                modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).graphicsLayer { rotationZ = 10f }
            )
            SwipeStamp(
                text = stringResource(R.string.pick),
                visible = y < -24f && abs(y) > abs(x),
                color = Color(0xFF90CAF9),
                modifier = Modifier.align(Alignment.TopCenter).padding(24.dp)
            )
            Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(22.dp)) {
                Text(
                    item.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SwipeBadge(item.format)
                    item.year?.let { SwipeBadge(it.toString()) }
                    item.rating?.let { SwipeBadge("★ " + "%.1f".format(it)) }
                }
                Text(
                    statusText,
                    color = Color.White.copy(alpha = .76f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SwipeBadge(text: String) {
    Surface(color = Color.Black.copy(alpha = .42f), shape = CircleShape) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), maxLines = 1)
    }
}

@Composable
private fun SwipeStamp(text: String, visible: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (visible) {
        Surface(modifier, color = Color.Black.copy(alpha = .28f), shape = RoundedCornerShape(10.dp), border = BorderStroke(3.dp, color)) {
            Text(text.uppercase(), color = color, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), maxLines = 1)
        }
    }
}

@Composable
private fun SwipeControls(
    canUndo: Boolean,
    onSkip: () -> Unit,
    onPick: () -> Unit,
    onShortlist: () -> Unit,
    onUndo: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        SwipeControl(stringResource(R.string.undo), Icons.AutoMirrored.Outlined.Undo, canUndo, onUndo, small = true)
        SwipeControl(stringResource(R.string.skip_media), Icons.Outlined.Close, true, onSkip)
        SwipeControl(stringResource(R.string.pick), Icons.Outlined.ArrowUpward, true, onPick, primary = true)
        SwipeControl(stringResource(R.string.shortlist), Icons.Outlined.Bookmarks, true, onShortlist)
    }
}

@Composable
private fun SwipeControl(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    small: Boolean = false,
    primary: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
        if (small) {
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            ) { Icon(icon, label) }
        } else {
            FloatingActionButton(
                onClick = onClick,
                containerColor = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
            ) { Icon(icon, label) }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun ShortlistDialog(items: List<MediaItem>, onDismiss: () -> Unit, onRemove: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.session_shortlist)) },
        text = {
            if (items.isEmpty()) {
                Text(stringResource(R.string.shortlist_empty))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.take(8).forEach { item ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            MediaCover(item, Modifier.size(48.dp, 64.dp), compact = true)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(item.format, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton({ onRemove(item.id) }) { Icon(Icons.Outlined.Close, stringResource(R.string.remove)) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.close)) } }
    )
}
