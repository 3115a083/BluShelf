package com.blushelf.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shelves
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blushelf.app.data.CsvImporter
import com.blushelf.app.data.MediaItem
import com.blushelf.app.data.MediaKind
import com.blushelf.app.data.MediaRepository
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

private const val MAX_IMPORT_BYTES = 10L * 1024L * 1024L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BluShelfApp() }
    }
}

class ShelfViewModel : ViewModel() {
    private val repository = MediaRepository()
    val items: List<MediaItem> get() = repository.items

    fun importCsv(text: String): Result<Int> = CsvImporter.parse(text).map { imported ->
        repository.replaceAll(imported)
        imported.size
    }
}

private enum class Destination { Shelf, Swipe, Settings }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BluShelfApp(shelfViewModel: ShelfViewModel = viewModel()) {
    var destination by rememberSaveable { mutableStateOf(Destination.Shelf) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val importSuccess = stringResource(R.string.import_success)
    val importFailure = stringResource(R.string.import_failure)
    val readFailure = stringResource(R.string.file_read_failure)
    val fileTooLarge = stringResource(R.string.file_too_large)
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            message = runCatching {
                val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
                require(size == null || size < 0 || size <= MAX_IMPORT_BYTES) { fileTooLarge }
                val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.readLimitedBytes(MAX_IMPORT_BYTES, fileTooLarge)
                } ?: throw IOException(readFailure)
                require(bytes.size <= MAX_IMPORT_BYTES) { fileTooLarge }
                bytes.toString(Charsets.UTF_8)
            }.fold(
                onSuccess = { csv ->
                    shelfViewModel.importCsv(csv).fold(
                        onSuccess = { count -> importSuccess.format(count) },
                        onFailure = { error -> "$importFailure: ${error.message ?: importFailure}" }
                    )
                },
                onFailure = { error -> error.message ?: readFailure }
            )
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(destination == Destination.Shelf, { destination = Destination.Shelf }, { Icon(Icons.Outlined.Shelves, null) }, label = { Text(stringResource(R.string.shelf)) })
                    NavigationBarItem(destination == Destination.Swipe, { destination = Destination.Swipe }, { Icon(Icons.Outlined.Swipe, null) }, label = { Text(stringResource(R.string.swipe)) })
                    NavigationBarItem(destination == Destination.Settings, { destination = Destination.Settings }, { Icon(Icons.Outlined.Settings, null) }, label = { Text(stringResource(R.string.settings)) })
                }
            }
        ) { padding ->
            when (destination) {
                Destination.Shelf -> ShelfScreen(shelfViewModel.items, { launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) }, Modifier.padding(padding))
                Destination.Swipe -> PlaceholderScreen(stringResource(R.string.swipe_not_implemented), Modifier.padding(padding))
                Destination.Settings -> PlaceholderScreen(stringResource(R.string.settings_not_implemented), Modifier.padding(padding))
            }
        }
    }
}

private fun InputStream.readLimitedBytes(limit: Long, limitMessage: String): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        total += count
        require(total <= limit) { limitMessage }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}

@Composable
private fun ShelfScreen(items: List<MediaItem>, onImport: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            Button(onClick = onImport) {
                Icon(Icons.Outlined.UploadFile, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(stringResource(R.string.import_csv))
            }
        }
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.empty_shelf), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.empty_shelf_hint), style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Text(stringResource(R.string.media_count, items.size), Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.titleMedium)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { MediaRow(it) }
            }
        }
    }
}

@Composable
private fun MediaRow(item: MediaItem) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(if (item.kind == MediaKind.VIDEO) Icons.Outlined.VideoFile else Icons.Outlined.AudioFile, contentDescription = stringResource(if (item.kind == MediaKind.VIDEO) R.string.video else R.string.audio))
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(listOfNotNull(item.year?.toString(), item.format.takeIf(String::isNotBlank)).joinToString(" • "), style = MaterialTheme.typography.bodyMedium)
                if (item.location.isNotBlank()) Text(stringResource(R.string.location_value, item.location), style = MaterialTheme.typography.bodySmall)
                item.rating?.let { Text(stringResource(R.string.rating_value, it), style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
