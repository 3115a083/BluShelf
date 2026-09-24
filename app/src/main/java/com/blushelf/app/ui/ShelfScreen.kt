package com.blushelf.app.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blushelf.app.R
import com.blushelf.app.ShelfKind
import com.blushelf.app.ShelfView
import com.blushelf.app.data.MediaItem
import com.blushelf.app.data.MediaKind
import com.blushelf.app.data.titleSortKey
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SortMode { TITLE, YEAR, RATING }
private enum class FilterMode { ALL, FAVORITES, UNPLAYED, WATCHLIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(
    allItems: List<MediaItem>, shelfKind: ShelfKind, view: ShelfView,
    onShelfKind: (ShelfKind) -> Unit, onView: (ShelfView) -> Unit,
    onImport: () -> Unit, onUnavailable: (String) -> Unit,
    onAdd: (String, MediaKind, String, Int?) -> Unit,
    onFavorite: (MediaItem) -> Unit, onPlayed: (MediaItem) -> Unit, onWatchlist: (MediaItem) -> Unit
) {
    val scannerUnavailable = stringResource(R.string.scanner_not_available)
    val metadataUnavailable = stringResource(R.string.metadata_not_configured)
    val detailsComing = stringResource(R.string.details_coming)
    var query by rememberSaveable { mutableStateOf("") }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var shelfMenu by remember { mutableStateOf(false) }
    var addMenu by remember { mutableStateOf(false) }
    var controlsOpen by remember { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf(SortMode.TITLE) }
    var filter by rememberSaveable { mutableStateOf(FilterMode.ALL) }
    var selected by remember { mutableStateOf<MediaItem?>(null) }
    var showManual by remember { mutableStateOf(false) }
    val kind = if (shelfKind == ShelfKind.VIDEO) MediaKind.VIDEO else MediaKind.AUDIO
    val shelfItems = allItems.filter { it.kind == kind }
    val visible = shelfItems.filter {
        it.title.contains(query, true) && when (filter) {
            FilterMode.ALL -> true
            FilterMode.FAVORITES -> it.favorite
            FilterMode.UNPLAYED -> !it.played
            FilterMode.WATCHLIST -> it.inWatchlist
        }
    }.let { values ->
        when (sort) {
            SortMode.TITLE -> values.sortedBy { titleSortKey(it.title) }
            SortMode.YEAR -> values.sortedWith(compareBy<MediaItem> { it.year ?: Int.MAX_VALUE }.thenBy { titleSortKey(it.title) })
            SortMode.RATING -> values.sortedWith(compareByDescending<MediaItem> { it.rating ?: 0f }.thenBy { titleSortKey(it.title) })
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Box {
                    TextButton(onClick = { shelfMenu = true }) { Text(if (shelfKind == ShelfKind.VIDEO) stringResource(R.string.video_shelf) else stringResource(R.string.audio_shelf), style = MaterialTheme.typography.titleLarge) }
                    DropdownMenu(shelfMenu, { shelfMenu = false }) {
                        DropdownMenuItem({ Text(stringResource(R.string.video_shelf)) }, { onShelfKind(ShelfKind.VIDEO); shelfMenu = false })
                        DropdownMenuItem({ Text(stringResource(R.string.audio_shelf)) }, { onShelfKind(ShelfKind.AUDIO); shelfMenu = false })
                    }
                }
            },
            actions = {
                IconButton({ searchVisible = !searchVisible }) { Icon(if (searchVisible) Icons.Outlined.Close else Icons.Outlined.Search, stringResource(R.string.search)) }
                IconButton({ controlsOpen = true }) {
                    Icon(
                        Icons.Outlined.Tune,
                        stringResource(R.string.shelf_options),
                        tint = if (filter == FilterMode.ALL) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
                }
                Box { FilledIconButton({ addMenu = true }) { Icon(Icons.Outlined.Add, stringResource(R.string.add)) }; DropdownMenu(addMenu, { addMenu = false }) {
                    DropdownMenuItem({ Text(stringResource(R.string.scan_barcode)) }, { addMenu = false; onUnavailable(scannerUnavailable) }, leadingIcon = { Icon(Icons.Outlined.QrCodeScanner, null) })
                    DropdownMenuItem({ Text(stringResource(R.string.search_online)) }, { addMenu = false; onUnavailable(metadataUnavailable) }, leadingIcon = { Icon(Icons.Outlined.Search, null) })
                    DropdownMenuItem({ Text(stringResource(R.string.add_manually)) }, { addMenu = false; showManual = true }, leadingIcon = { Icon(Icons.Outlined.Add, null) })
                    DropdownMenuItem({ Text(stringResource(R.string.import_csv)) }, { addMenu = false; onImport() }, leadingIcon = { Icon(Icons.Outlined.UploadFile, null) })
                } }
            }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (searchVisible) OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), placeholder = { Text(stringResource(R.string.search_collection)) }, singleLine = true)
            if (visible.isEmpty() && shelfItems.isNotEmpty()) {
                FilteredEmpty { query = ""; filter = FilterMode.ALL }
            } else if (visible.isEmpty()) EmptyShelf(onImport, { showManual = true }, onUnavailable)
            else when (view) {
                ShelfView.VIRTUAL -> VirtualShelf(visible, selected, sort == SortMode.TITLE, { selected = it })
                ShelfView.LIST -> SimpleList(visible) { selected = it }
                ShelfView.DETAILED -> DetailedList(visible) { selected = it }
            }
        }
    }
    selected?.let { item -> QuickView(item, { selected = null }, { onFavorite(item) }, { onPlayed(item) }, { onWatchlist(item) }, { onUnavailable(detailsComing) }) }
    if (showManual) ManualAddDialog(kind, { showManual = false }, { title, format, year -> onAdd(title, kind, format, year); showManual = false })
    if (controlsOpen) {
        ShelfControlsSheet(
            sort = sort,
            filter = filter,
            view = view,
            onSort = { sort = it },
            onFilter = { filter = it },
            onView = onView,
            onDismiss = { controlsOpen = false }
        )
    }
}

@Composable
private fun FilteredEmpty(onClear: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.FilterList, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.no_filter_results), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        TextButton(onClear, Modifier.padding(top = 8.dp)) { Text(stringResource(R.string.clear_filters)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfControlsSheet(
    sort: SortMode,
    filter: FilterMode,
    view: ShelfView,
    onSort: (SortMode) -> Unit,
    onFilter: (FilterMode) -> Unit,
    onView: (ShelfView) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            stringResource(R.string.shelf_options),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        SheetSection(stringResource(R.string.sort_by)) {
            SheetChoice(stringResource(R.string.sort_title), sort == SortMode.TITLE) { onSort(SortMode.TITLE) }
            SheetChoice(stringResource(R.string.sort_year), sort == SortMode.YEAR) { onSort(SortMode.YEAR) }
            SheetChoice(stringResource(R.string.sort_rating), sort == SortMode.RATING) { onSort(SortMode.RATING) }
        }
        SheetSection(stringResource(R.string.filter_by)) {
            SheetChoice(stringResource(R.string.show_all), filter == FilterMode.ALL) { onFilter(FilterMode.ALL) }
            SheetChoice(stringResource(R.string.favorite), filter == FilterMode.FAVORITES) { onFilter(FilterMode.FAVORITES) }
            SheetChoice(stringResource(R.string.unplayed_only), filter == FilterMode.UNPLAYED) { onFilter(FilterMode.UNPLAYED) }
            SheetChoice(stringResource(R.string.watchlist), filter == FilterMode.WATCHLIST) { onFilter(FilterMode.WATCHLIST) }
        }
        SheetSection(stringResource(R.string.view_as)) {
            SheetChoice(stringResource(R.string.virtual_shelf), view == ShelfView.VIRTUAL) { onView(ShelfView.VIRTUAL) }
            SheetChoice(stringResource(R.string.list), view == ShelfView.LIST) { onView(ShelfView.LIST) }
            SheetChoice(stringResource(R.string.detailed), view == ShelfView.DETAILED) { onView(ShelfView.DETAILED) }
        }
        Button(onDismiss, Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp)) {
            Text(stringResource(R.string.done))
        }
    }
}

@Composable
private fun SheetSection(title: String, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp)
    )
    content()
}

@Composable
private fun SheetChoice(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(title, Modifier.padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyShelf(onImport: () -> Unit, onManual: () -> Unit, onUnavailable: (String) -> Unit) {
    val scannerUnavailable = stringResource(R.string.scanner_not_available)
    val metadataUnavailable = stringResource(R.string.metadata_not_configured)
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.GridView, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.empty_shelf), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
        Text(stringResource(R.string.empty_shelf_product_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        Button({ onUnavailable(scannerUnavailable) }) { Icon(Icons.Outlined.QrCodeScanner, null); Text(stringResource(R.string.scan_barcode), Modifier.padding(start = 8.dp)) }
        OutlinedButton({ onUnavailable(metadataUnavailable) }, Modifier.padding(top = 10.dp).widthIn(min = 220.dp)) { Text(stringResource(R.string.search_online), maxLines = 1) }
        OutlinedButton(onManual, Modifier.padding(top = 8.dp).widthIn(min = 220.dp)) { Text(stringResource(R.string.add_manually), maxLines = 1) }
        TextButton(onImport, Modifier.padding(top = 8.dp)) { Icon(Icons.Outlined.UploadFile, null); Text(stringResource(R.string.import_collection), Modifier.padding(start = 8.dp)) }
    }
}

@Composable
private fun VirtualShelf(items: List<MediaItem>, selected: MediaItem?, alphabetNavigation: Boolean, onSelect: (MediaItem) -> Unit) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val currentLetter = items.getOrNull(listState.firstVisibleItemIndex)?.let { titleSortKey(it.title).firstOrNull()?.uppercase() } ?: "#"
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stringResource(R.string.physical_collection), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.media_count, items.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(currentLetter, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
            }
        }
        Surface(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 10.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    items(items, key = { it.id }) { item -> MediaSpine(item, selected?.id == item.id) { onSelect(item) } }
                }
                Box(Modifier.fillMaxWidth().height(14.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .62f)))
                Box(Modifier.fillMaxWidth().height(7.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .28f)))
            }
        }
        if (alphabetNavigation) {
            Text(stringResource(R.string.scrubber_hint), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 18.dp, top = 8.dp))
            AlphabetScrubber(currentLetter, items) { letter ->
                val index = items.indexOfFirst { titleSortKey(it.title).startsWith(letter, true) }
                if (index >= 0) scope.launch { listState.scrollToItem(index) }
            }
        }
    }
}

@Composable
private fun AlphabetScrubber(current: String, items: List<MediaItem>, onLetter: (Char) -> Unit) {
    val letters = ('A'..'Z').toList()
    val enabled = remember(items) { letters.associateWith { letter -> items.any { titleSortKey(it.title).startsWith(letter, true) } } }
    val currentIndex = letters.indexOfFirst { it.toString() == current }.coerceAtLeast(0)
    var touchedIndex by remember { mutableStateOf<Int?>(null) }
    fun nearestEnabled(index: Int): Int = letters.indices
        .filter { enabled[letters[it]] == true }
        .minByOrNull { kotlin.math.abs(it - index) } ?: index
    Box(
        Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 8.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .pointerInput(items) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val started = System.currentTimeMillis()
                    val cellWidth = size.width / letters.size
                    fun rawIndex(x: Float) = (x / cellWidth).toInt().coerceIn(0, letters.lastIndex)
                    var target = nearestEnabled(rawIndex(down.position.x))
                    var precisionAnchorX = down.position.x
                    var precisionAnchorIndex = target
                    var precisionStarted = false
                    touchedIndex = target
                    onLetter(letters[target])
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val precise = System.currentTimeMillis() - started >= 350L
                        if (precise && !precisionStarted) {
                            precisionStarted = true
                            precisionAnchorX = change.position.x
                            precisionAnchorIndex = target
                        }
                        val rawTarget = if (precise) {
                            (precisionAnchorIndex + ((change.position.x - precisionAnchorX) / cellWidth) * .38f)
                                .roundToInt().coerceIn(0, letters.lastIndex)
                        } else rawIndex(change.position.x)
                        val next = nearestEnabled(rawTarget)
                        if (next != target) {
                            target = next
                            touchedIndex = target
                            onLetter(letters[target])
                        }
                        change.consume()
                    }
                    touchedIndex = null
                }
            }
    ) {
        val focus = touchedIndex ?: currentIndex
        Row(Modifier.fillMaxSize().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            letters.forEachIndexed { index, letter ->
                val active = focus == index
                val available = enabled[letter] == true
                val distance = kotlin.math.abs(index - focus)
                val targetScale = when (distance) { 0 -> 2f; 1 -> 1.55f; 2 -> 1.25f; 3 -> 1.1f; else -> 1f }
                val scale by animateFloatAsState(if (available) targetScale else 1f, label = "letter magnification")
                Box(
                    Modifier.weight(1f).fillMaxHeight().zIndex(scale).graphicsLayer { scaleX = scale; scaleY = scale },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        letter.toString(),
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Black else FontWeight.Medium,
                        color = when { active -> MaterialTheme.colorScheme.primary; available -> MaterialTheme.colorScheme.onSurfaceVariant; else -> MaterialTheme.colorScheme.outlineVariant }
                    )
                    if (active) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).size(4.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
private fun MediaSpine(item: MediaItem, selected: Boolean, onClick: () -> Unit) {
    val (spineWidth, spineHeight) = formatSize(item.format)
    val lift by animateDpAsState(if (selected) (-18).dp else 0.dp, label = "spine lift")
    val color = formatColor(item.format)
    Box(Modifier.width(maxOf(50.dp, spineWidth + 14.dp)).requiredHeight(258.dp).clickable(onClick = onClick).semantics { contentDescription = item.title + ", " + item.format }, contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.offset(y = lift).width(spineWidth).height(spineHeight).clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)).background(color).border(1.dp, Color.White.copy(alpha = .2f), RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)).animateContentSize(), contentAlignment = Alignment.Center) {
            Text(item.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.rotate(-90f).width(spineHeight - 34.dp))
            Text(item.format.take(3).uppercase(), color = Color.White.copy(alpha = .78f), fontSize = 8.sp, maxLines = 1, modifier = Modifier.align(Alignment.BottomCenter).padding(3.dp))
        }
    }
}

private fun formatSize(format: String): Pair<Dp, Dp> = when {
    format.contains("VHS", true) -> 54.dp to 218.dp
    format.contains("DVD", true) -> 40.dp to 226.dp
    format.contains("UHD", true) -> 32.dp to 202.dp
    format.contains("Blu", true) -> 30.dp to 198.dp
    format.contains("Vinyl", true) -> 18.dp to 238.dp
    format.contains("CD", true) -> 24.dp to 164.dp
    else -> 30.dp to 198.dp
}

private fun formatColor(format: String): Color = when {
    format.contains("UHD", true) -> Color(0xFF263238)
    format.contains("Blu", true) -> Color(0xFF1565C0)
    format.contains("DVD", true) -> Color(0xFF6A1B9A)
    format.contains("VHS", true) -> Color(0xFF4E342E)
    format.contains("Vinyl", true) -> Color(0xFF00695C)
    format.contains("CD", true) -> Color(0xFFC62828)
    else -> Color(0xFF455A64)
}

@Composable
private fun SimpleList(items: List<MediaItem>, onClick: (MediaItem) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) { items(items, key = { it.id }) { item -> ListItem({ Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis) }, Modifier.clickable { onClick(item) }, supportingContent = { Text(listOfNotNull(item.year?.toString(), item.format).joinToString(" · "), maxLines = 1) }, leadingContent = { MediaCover(item, Modifier.size(42.dp, 58.dp), compact = true) }); HorizontalDivider() } }
}

@Composable
private fun DetailedList(items: List<MediaItem>, onClick: (MediaItem) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(items, key = { it.id }) { item ->
        Card(Modifier.fillMaxWidth().clickable { onClick(item) }) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            MediaCover(item, Modifier.size(58.dp, 82.dp), compact = true)
            Column(Modifier.padding(start = 14.dp).weight(1f)) { Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(listOfNotNull(item.year?.toString(), item.format).joinToString(" · "), maxLines = 1); if (item.location.isNotBlank()) Text(item.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis); item.rating?.let { Text("★ %.1f".format(it), color = MaterialTheme.colorScheme.primary) } }
            if (item.favorite) Icon(Icons.Filled.Favorite, null, tint = MaterialTheme.colorScheme.primary)
        } }
    } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickView(item: MediaItem, onDismiss: () -> Unit, onFavorite: () -> Unit, onPlayed: () -> Unit, onWatchlist: () -> Unit, onDetails: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MediaCover(item, Modifier.size(120.dp, 170.dp))
            Column(Modifier.weight(1f)) { Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis); Text(listOfNotNull(item.year?.toString(), item.format).joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1); item.rating?.let { Text("★ %.1f / 5".format(it), modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.primary) }; if (item.location.isNotBlank()) Text(item.location, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp), maxLines = 2, overflow = TextOverflow.Ellipsis) }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(item.played, onPlayed, { Text(stringResource(if (item.kind == MediaKind.VIDEO) R.string.watched else R.string.listened)) }, leadingIcon = { Icon(Icons.Outlined.CheckCircle, null) })
            FilterChip(item.favorite, onFavorite, { Text(stringResource(R.string.favorite)) }, leadingIcon = { Icon(if (item.favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null) })
            FilterChip(item.inWatchlist, onWatchlist, { Text(stringResource(if (item.kind == MediaKind.VIDEO) R.string.watchlist else R.string.listen_list)) }, leadingIcon = { Icon(Icons.Outlined.WatchLater, null) })
        }
        Button(onDetails, Modifier.fillMaxWidth().padding(16.dp)) { Text(stringResource(R.string.details)) }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ManualAddDialog(kind: MediaKind, onDismiss: () -> Unit, onAdd: (String, String, Int?) -> Unit) {
    var title by remember { mutableStateOf("") }; var format by remember { mutableStateOf(if (kind == MediaKind.VIDEO) "Blu-ray" else "CD") }; var year by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.add_manually)) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.title)) }); OutlinedTextField(format, { format = it }, label = { Text(stringResource(R.string.format)) }); OutlinedTextField(year, { year = it.filter(Char::isDigit).take(4) }, label = { Text(stringResource(R.string.year)) }) } }, confirmButton = { TextButton({ if (title.isNotBlank() && format.isNotBlank()) onAdd(title.trim(), format.trim(), year.toIntOrNull()) }, enabled = title.isNotBlank() && format.isNotBlank()) { Text(stringResource(R.string.add)) } }, dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.cancel)) } })
}
