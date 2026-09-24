package com.blushelf.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blushelf.app.ColorPalette
import com.blushelf.app.R
import com.blushelf.app.ShelfKind
import com.blushelf.app.ShelfView
import com.blushelf.app.ThemeMode
import com.blushelf.app.data.MediaKind
import com.blushelf.app.data.ShelfEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    theme: ThemeMode,
    dynamic: Boolean,
    palette: ColorPalette,
    shelves: List<ShelfEntity>,
    defaultView: ShelfView,
    defaultShelf: ShelfKind,
    wishlistGhosts: Boolean,
    batchScanning: Boolean,
    haptic: Boolean,
    onTheme: (ThemeMode) -> Unit,
    onDynamic: (Boolean) -> Unit,
    onPalette: (ColorPalette) -> Unit,
    onDefaultView: (ShelfView) -> Unit,
    onDefaultShelf: (ShelfKind) -> Unit,
    onCreateShelf: (String, MediaKind) -> Unit,
    onDeleteShelf: (ShelfEntity) -> Unit,
    onWishlistGhosts: (Boolean) -> Unit,
    onBatchScanning: (Boolean) -> Unit,
    onHaptic: (Boolean) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLanguage: () -> Unit,
    onMessage: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var detail by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showShelves by remember { mutableStateOf(false) }
    var showCreateShelf by remember { mutableStateOf(false) }
    var deleteShelf by remember { mutableStateOf<ShelfEntity?>(null) }
    var showImportExport by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val notConfigured = stringResource(R.string.not_configured)
    val notAvailable = stringResource(R.string.prepared_not_available)
    val localOnly = stringResource(R.string.local_first_summary)
    val providerInfo = stringResource(R.string.provider_setup_later)
    val serverInfo = stringResource(R.string.server_setup_later)
    val cacheEmpty = stringResource(R.string.cache_empty)
    val scannerUnavailable = stringResource(R.string.scanner_not_available)
    val syncStatus = stringResource(R.string.sync_status)
    val webdavInfo = stringResource(R.string.webdav_setup_later)
    val backupInfo = stringResource(R.string.backup_setup_later)
    val scannerTitle = stringResource(R.string.scanner)
    val noTracking = stringResource(R.string.no_tracking_summary)
    val networkSummary = stringResource(R.string.network_on_use_summary)
    val aboutBluShelf = stringResource(R.string.about_blushelf)

    fun matches(vararg text: String) = query.isBlank() || text.any { it.contains(query, true) }
    fun show(title: String, body: String) { detail = title to body }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) }) }) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp)
        ) {
            item {
                OutlinedTextField(
                    query,
                    { query = it },
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    placeholder = { Text(stringResource(R.string.search_settings)) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp)
                )
            }

            if (matches("Darstellung", "Appearance", "Theme", "Farbe", "Sprache")) item {
                SettingsSection(stringResource(R.string.appearance), Icons.Outlined.Brush) {
                    SettingLabel(stringResource(R.string.theme))
                    ThemeMode.entries.forEach { mode ->
                        ChoiceRow(
                            selected = theme == mode,
                            title = stringResource(when (mode) {
                                ThemeMode.SYSTEM -> R.string.theme_system
                                ThemeMode.LIGHT -> R.string.theme_light
                                ThemeMode.DARK -> R.string.theme_dark
                            }),
                            onClick = { onTheme(mode) }
                        )
                    }
                    HorizontalDivider()
                    SwitchRow(stringResource(R.string.dynamic_color), stringResource(R.string.dynamic_color_summary), dynamic, onDynamic, Icons.Outlined.ColorLens)
                    SettingLabel(stringResource(R.string.color_palette))
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorPalette.entries.forEach { value ->
                            FilterChip(
                                selected = palette == value,
                                onClick = { onPalette(value) },
                                enabled = !dynamic,
                                label = { Text(value.name.lowercase().replaceFirstChar(Char::uppercase), maxLines = 1) },
                                leadingIcon = { Icon(Icons.Outlined.ColorLens, null, tint = paletteColor(value)) }
                            )
                        }
                    }
                    SettingsRow(stringResource(R.string.language), stringResource(R.string.system_language_hint), Icons.Outlined.Language, onLanguage)
                    SettingLabel(stringResource(R.string.default_shelf_view))
                    ShelfView.entries.forEach { value ->
                        ChoiceRow(
                            selected = defaultView == value,
                            title = stringResource(when (value) {
                                ShelfView.VIRTUAL -> R.string.virtual_shelf
                                ShelfView.LIST -> R.string.list
                                ShelfView.DETAILED -> R.string.detailed
                            }),
                            onClick = { onDefaultView(value) }
                        )
                    }
                }
            }

            if (matches("Sammlung", "Shelves", "Custom", "Standorte", "Tags", "Wishlist")) item {
                val custom = stringResource(R.string.custom_fields)
                val locations = stringResource(R.string.locations)
                val tags = stringResource(R.string.tags_collections)
                SettingsSection(stringResource(R.string.collection), Icons.Outlined.Storage) {
                    SettingsRow(stringResource(R.string.manage_shelves), stringResource(R.string.custom_shelf_count, shelves.size), Icons.Outlined.Storage) { showShelves = true }
                    SettingsRow(custom, notAvailable, Icons.Outlined.Tune) { show(custom, notAvailable) }
                    SettingsRow(locations, notAvailable, Icons.Outlined.Folder) { show(locations, notAvailable) }
                    SettingsRow(tags, notAvailable, Icons.Outlined.Tag) { show(tags, notAvailable) }
                    SwitchRow(stringResource(R.string.wishlist_ghosts), stringResource(R.string.wishlist_ghosts_summary), wishlistGhosts, onWishlistGhosts, Icons.Outlined.Visibility)
                }
            }

            if (matches("Metadaten", "TMDB", "MusicBrainz")) item {
                SettingsSection(stringResource(R.string.metadata), Icons.Outlined.Movie) {
                    SettingsRow("TMDB", notConfigured, Icons.Outlined.CloudOff) { show("TMDB", providerInfo) }
                    SettingsRow("MusicBrainz / Cover Art Archive", notConfigured, Icons.Outlined.CloudOff) { show("MusicBrainz / Cover Art Archive", providerInfo) }
                }
            }
            if (matches("Server", "Plex", "Jellyfin", "Navidrome")) item {
                SettingsSection(stringResource(R.string.digital_servers), Icons.Outlined.Storage) {
                    listOf("Plex", "Jellyfin", "Navidrome / OpenSubsonic").forEach { server ->
                        SettingsRow(server, notConfigured, Icons.Outlined.CloudOff) { show(server, serverInfo) }
                    }
                }
            }
            if (matches("Sync", "WebDAV", "Backup", "Import", "Export")) item {
                val webdav = "WebDAV"
                val backup = stringResource(R.string.backup)
                SettingsSection(stringResource(R.string.sync_backup), Icons.Outlined.Sync) {
                    SettingsRow(syncStatus, stringResource(R.string.local_only), Icons.Outlined.CloudOff) { show(syncStatus, localOnly) }
                    SettingsRow(webdav, notConfigured, Icons.Outlined.Sync) { show(webdav, webdavInfo) }
                    SettingsRow(backup, stringResource(R.string.not_available_yet), Icons.Outlined.Backup) { show(backup, backupInfo) }
                    SettingsRow(stringResource(R.string.import_export), stringResource(R.string.csv_import_export_available), Icons.Outlined.Storage) { showImportExport = true }
                }
            }
            if (matches("Scanner", "Batch", "Haptik")) item {
                SettingsSection(stringResource(R.string.scanner), Icons.Outlined.QrCodeScanner) {
                    SettingsRow(stringResource(R.string.scanner_status), scannerUnavailable, Icons.Outlined.Info) { show(scannerTitle, scannerUnavailable) }
                    SwitchRow(stringResource(R.string.batch_scanning), stringResource(R.string.batch_scanning_summary), batchScanning, onBatchScanning, Icons.Outlined.QrCodeScanner)
                    SwitchRow(stringResource(R.string.haptic_confirmation), stringResource(R.string.applies_when_scanner_available), haptic, onHaptic, Icons.Outlined.Settings)
                }
            }
            if (matches("Datenschutz", "Sicherheit", "Telemetrie", "Werbung", "Cache")) item {
                val privacy = stringResource(R.string.local_first)
                val telemetry = stringResource(R.string.no_telemetry_ads)
                val network = stringResource(R.string.network_on_use)
                SettingsSection(stringResource(R.string.privacy_security), Icons.Outlined.Security) {
                    SettingsRow(privacy, stringResource(R.string.local_first_summary), Icons.Outlined.Lock) { show(privacy, localOnly) }
                    SettingsRow(telemetry, noTracking, Icons.Outlined.Security) { show(telemetry, noTracking) }
                    SettingsRow(network, networkSummary, Icons.Outlined.CloudOff) { show(network, networkSummary) }
                    SettingsRow(stringResource(R.string.clear_cache), stringResource(R.string.local_action), Icons.Outlined.Storage) { onMessage(cacheEmpty) }
                }
            }
            if (matches("Über", "About", "Version", "Open Source")) item {
                SettingsSection(stringResource(R.string.about), Icons.Outlined.Code) {
                    SettingsRow("BluShelf", "Version 0.5.0 · Debug", Icons.Outlined.Info) { show("BluShelf", aboutBluShelf) }
                    SettingsRow(stringResource(R.string.open_source_project), "github.com/3115a083/BluShelf", Icons.Outlined.Code) {
                        uriHandler.openUri("https://github.com/3115a083/BluShelf")
                    }
                }
            }
        }
    }

    detail?.let { value ->
        AlertDialog(
            onDismissRequest = { detail = null },
            icon = { Icon(Icons.Outlined.Info, null) },
            title = { Text(value.first) },
            text = { Text(value.second) },
            confirmButton = { TextButton({ detail = null }) { Text(stringResource(R.string.close)) } }
        )
    }
    if (showShelves) {
        AlertDialog(
            onDismissRequest = { showShelves = false },
            title = { Text(stringResource(R.string.manage_shelves)) },
            text = {
                Column {
                    Text(stringResource(R.string.default_shelf), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 6.dp))
                    ChoiceRow(defaultShelf == ShelfKind.VIDEO, stringResource(R.string.video_shelf)) { onDefaultShelf(ShelfKind.VIDEO) }
                    ChoiceRow(defaultShelf == ShelfKind.AUDIO, stringResource(R.string.audio_shelf)) { onDefaultShelf(ShelfKind.AUDIO) }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 240.dp)) {
                        items(shelves.size) { index ->
                            val shelf = shelves[index]
                            ListItem(
                                headlineContent = { Text(shelf.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                supportingContent = { Text(stringResource(if (shelf.kind == MediaKind.VIDEO.name) R.string.video_shelf else R.string.audio_shelf)) },
                                trailingContent = {
                                    if (!shelf.builtIn) IconButton({ deleteShelf = shelf }) { Icon(Icons.Outlined.Delete, stringResource(R.string.delete_shelf)) }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton({ showShelves = false; showCreateShelf = true }) { Text(stringResource(R.string.add_shelf)) } },
            dismissButton = { TextButton({ showShelves = false }) { Text(stringResource(R.string.done)) } }
        )
    }
    if (showCreateShelf) {
        ShelfCreateDialog(
            initialKind = if (defaultShelf == ShelfKind.VIDEO) MediaKind.VIDEO else MediaKind.AUDIO,
            onDismiss = { showCreateShelf = false },
            onCreate = { name, kind -> onCreateShelf(name, kind); showCreateShelf = false }
        )
    }
    deleteShelf?.let { shelf ->
        AlertDialog(
            onDismissRequest = { deleteShelf = null },
            title = { Text(stringResource(R.string.delete_shelf)) },
            text = { Text(stringResource(R.string.delete_shelf_confirmation, shelf.name)) },
            confirmButton = { TextButton({ onDeleteShelf(shelf); deleteShelf = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton({ deleteShelf = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (showImportExport) {
        AlertDialog(
            onDismissRequest = { showImportExport = false },
            title = { Text(stringResource(R.string.import_export)) },
            text = { Text(stringResource(R.string.import_export_explanation)) },
            confirmButton = {
                Button({ showImportExport = false; onImport() }) { Text(stringResource(R.string.import_csv), maxLines = 1) }
            },
            dismissButton = {
                OutlinedButton({ showImportExport = false; onExport() }) { Text(stringResource(R.string.export_csv), maxLines = 1) }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        content()
        HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
    }
}

@Composable
private fun SettingLabel(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp), maxLines = 1)
}

@Composable
private fun ChoiceRow(selected: Boolean, title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(title, Modifier.padding(start = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ShelfCreateDialog(
    initialKind: MediaKind,
    onDismiss: () -> Unit,
    onCreate: (String, MediaKind) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(initialKind) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_shelf)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.shelf_name)) }, singleLine = true)
                Text(stringResource(R.string.shelf_type), style = MaterialTheme.typography.labelLarge)
                listOf(MediaKind.VIDEO, MediaKind.AUDIO).forEach { value ->
                    ChoiceRow(
                        selected = kind == value,
                        title = stringResource(if (value == MediaKind.VIDEO) R.string.video_shelf else R.string.audio_shelf)
                    ) { kind = value }
                }
            }
        },
        confirmButton = { TextButton({ onCreate(name.trim(), kind) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.create_shelf)) } },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun SwitchRow(title: String, summary: String, checked: Boolean, onChecked: (Boolean) -> Unit, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier.clickable { onChecked(!checked) },
        supportingContent = { Text(summary, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Switch(checked, onCheckedChange = onChecked) }
    )
}

@Composable
private fun SettingsRow(title: String, summary: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier.clickable(onClick = onClick),
        supportingContent = { Text(summary, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.outline) }
    )
}

private fun paletteColor(value: ColorPalette) = when (value) {
    ColorPalette.OCEAN -> Color(0xFF00639A)
    ColorPalette.INDIGO -> Color(0xFF4555A5)
    ColorPalette.TEAL -> Color(0xFF006A67)
}
