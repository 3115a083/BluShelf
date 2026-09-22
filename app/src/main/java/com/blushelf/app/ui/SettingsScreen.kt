package com.blushelf.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Folder
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blushelf.app.ColorPalette
import com.blushelf.app.R
import com.blushelf.app.ShelfView
import com.blushelf.app.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    theme: ThemeMode, dynamic: Boolean, palette: ColorPalette, defaultView: ShelfView,
    wishlistGhosts: Boolean, batchScanning: Boolean, haptic: Boolean,
    onTheme: (ThemeMode) -> Unit, onDynamic: (Boolean) -> Unit, onPalette: (ColorPalette) -> Unit,
    onDefaultView: (ShelfView) -> Unit, onWishlistGhosts: (Boolean) -> Unit,
    onBatchScanning: (Boolean) -> Unit, onHaptic: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    fun matches(vararg text: String) = query.isBlank() || text.any { it.contains(query, true) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(16.dp), leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text(stringResource(R.string.search_settings)) }, singleLine = true) }

            if (matches("Darstellung", "Appearance", "Theme", "Farbe", "Sprache")) item {
                SettingsSection(stringResource(R.string.appearance), Icons.Outlined.Brush) {
                    Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
                    ThemeMode.entries.forEach { mode -> ChoiceRow(theme == mode, stringResource(when (mode) { ThemeMode.SYSTEM -> R.string.theme_system; ThemeMode.LIGHT -> R.string.theme_light; ThemeMode.DARK -> R.string.theme_dark })) { onTheme(mode) } }
                    SwitchRow(stringResource(R.string.dynamic_color), stringResource(R.string.dynamic_color_summary), dynamic, onDynamic, Icons.Outlined.ColorLens)
                    Text(stringResource(R.string.color_palette), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 6.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { ColorPalette.entries.forEach { value -> AssistChip({ onPalette(value) }, { Text(value.name.lowercase().replaceFirstChar(Char::uppercase)) }, leadingIcon = { Icon(Icons.Outlined.ColorLens, null, tint = paletteColor(value)) }) } }
                    StatusRow(stringResource(R.string.language), stringResource(R.string.system_language_hint), Icons.Outlined.Language)
                    Text(stringResource(R.string.default_shelf_view), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
                    ShelfView.entries.forEach { value -> ChoiceRow(defaultView == value, stringResource(when (value) { ShelfView.VIRTUAL -> R.string.virtual_shelf; ShelfView.LIST -> R.string.list; ShelfView.DETAILED -> R.string.detailed })) { onDefaultView(value) } }
                }
            }

            if (matches("Sammlung", "Shelves", "Custom", "Standorte", "Tags", "Wishlist")) item {
                SettingsSection(stringResource(R.string.collection), Icons.Outlined.Storage) {
                    StatusRow(stringResource(R.string.manage_shelves), stringResource(R.string.video_audio_shelves), Icons.Outlined.Storage)
                    StatusRow(stringResource(R.string.custom_fields), stringResource(R.string.prepared_not_available), Icons.Outlined.Tune)
                    StatusRow(stringResource(R.string.locations), stringResource(R.string.prepared_not_available), Icons.Outlined.Folder)
                    StatusRow(stringResource(R.string.tags_collections), stringResource(R.string.prepared_not_available), Icons.Outlined.Tag)
                    SwitchRow(stringResource(R.string.wishlist_ghosts), stringResource(R.string.wishlist_ghosts_summary), wishlistGhosts, onWishlistGhosts, Icons.Outlined.Visibility)
                }
            }

            if (matches("Metadaten", "TMDB", "MusicBrainz")) item { SettingsSection(stringResource(R.string.metadata), Icons.Outlined.Movie) { StatusRow("TMDB", stringResource(R.string.not_configured), Icons.Outlined.CloudOff); StatusRow("MusicBrainz / Cover Art Archive", stringResource(R.string.not_configured), Icons.Outlined.CloudOff) } }
            if (matches("Server", "Plex", "Jellyfin", "Navidrome")) item { SettingsSection(stringResource(R.string.digital_servers), Icons.Outlined.Storage) { StatusRow("Plex", stringResource(R.string.not_configured), Icons.Outlined.CloudOff); StatusRow("Jellyfin", stringResource(R.string.not_configured), Icons.Outlined.CloudOff); StatusRow("Navidrome / OpenSubsonic", stringResource(R.string.not_configured), Icons.Outlined.CloudOff) } }
            if (matches("Sync", "WebDAV", "Backup", "Import", "Export")) item { SettingsSection(stringResource(R.string.sync_backup), Icons.Outlined.Sync) { StatusRow(stringResource(R.string.sync_status), stringResource(R.string.local_only), Icons.Outlined.CloudOff); StatusRow("WebDAV", stringResource(R.string.not_configured), Icons.Outlined.Sync); StatusRow(stringResource(R.string.backup), stringResource(R.string.not_available_yet), Icons.Outlined.Backup); StatusRow(stringResource(R.string.import_export), stringResource(R.string.csv_import_available), Icons.Outlined.Storage) } }
            if (matches("Scanner", "Batch", "Haptik")) item { SettingsSection(stringResource(R.string.scanner), Icons.Outlined.QrCodeScanner) { SwitchRow(stringResource(R.string.batch_scanning), stringResource(R.string.scanner_not_available), batchScanning, onBatchScanning, Icons.Outlined.QrCodeScanner); SwitchRow(stringResource(R.string.haptic_confirmation), stringResource(R.string.applies_when_scanner_available), haptic, onHaptic, Icons.Outlined.Settings) } }
            if (matches("Datenschutz", "Sicherheit", "Telemetrie", "Werbung", "Cache")) item { SettingsSection(stringResource(R.string.privacy_security), Icons.Outlined.Security) { StatusRow(stringResource(R.string.local_first), stringResource(R.string.local_first_summary), Icons.Outlined.Lock); StatusRow(stringResource(R.string.no_telemetry_ads), stringResource(R.string.no_tracking_summary), Icons.Outlined.Security); StatusRow(stringResource(R.string.network_on_use), stringResource(R.string.network_on_use_summary), Icons.Outlined.CloudOff); ListItem({ Text(stringResource(R.string.clear_cache)) }, Modifier.clickable { onMessage("Kein heruntergeladener Artwork-Cache vorhanden") }, supportingContent = { Text(stringResource(R.string.local_action)) }, leadingContent = { Icon(Icons.Outlined.Storage, null) }) } }
            if (matches("Über", "About", "Version", "Open Source")) item { SettingsSection(stringResource(R.string.about), Icons.Outlined.Code) { StatusRow("BluShelf", "Version 0.2.0 · Debug", Icons.Outlined.Code); StatusRow(stringResource(R.string.open_source_project), "github.com/3115a083/BluShelf", Icons.Outlined.Code) } }
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp)) {
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        Card(Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun ChoiceRow(selected: Boolean, title: String, onClick: () -> Unit) { ListItem({ Text(title) }, Modifier.clickable(onClick = onClick), leadingContent = { RadioButton(selected, onClick) }) }

@Composable
private fun SwitchRow(title: String, summary: String, checked: Boolean, onChecked: (Boolean) -> Unit, icon: ImageVector) { ListItem({ Text(title) }, Modifier.clickable { onChecked(!checked) }, supportingContent = { Text(summary) }, leadingContent = { Icon(icon, null) }, trailingContent = { Switch(checked, onCheckedChange = onChecked) }) }

@Composable
private fun StatusRow(title: String, summary: String, icon: ImageVector) { ListItem({ Text(title) }, supportingContent = { Text(summary) }, leadingContent = { Icon(icon, null) }) }

private fun paletteColor(value: ColorPalette) = when (value) { ColorPalette.OCEAN -> Color(0xFF00639A); ColorPalette.INDIGO -> Color(0xFF4555A5); ColorPalette.TEAL -> Color(0xFF006A67) }
