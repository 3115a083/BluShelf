package com.blushelf.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blushelf.app.ui.BluShelfTheme
import com.blushelf.app.ui.OnboardingScreen
import com.blushelf.app.ui.SettingsScreen
import com.blushelf.app.ui.ShelfScreen
import com.blushelf.app.ui.SwipeScreen
import com.blushelf.app.data.CsvExporter
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

private const val MAX_IMPORT_BYTES = 10L * 1024L * 1024L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BluShelfRoot() }
    }
}

private enum class Destination { SHELF, SWIPE, SETTINGS }

@Composable
private fun BluShelfRoot(app: BluShelfViewModel = viewModel()) {
    BluShelfTheme(app.themeMode, app.dynamicColor, app.palette) {
        val items by app.items.collectAsStateWithLifecycle()
        val context = LocalContext.current
        var destination by rememberSaveable { mutableStateOf(Destination.SHELF) }
        var message by remember { mutableStateOf<String?>(null) }
        var finishOnImport by remember { mutableStateOf(false) }
        val snackbar = remember { SnackbarHostState() }
        val importSuccess = stringResource(R.string.import_success)
        val importFailure = stringResource(R.string.import_failure)
        val readFailure = stringResource(R.string.file_read_failure)
        val fileTooLarge = stringResource(R.string.file_too_large)
        val exportSuccess = stringResource(R.string.export_success)
        val exportFailure = stringResource(R.string.export_failure)

        val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                runCatching {
                    val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
                    require(size == null || size < 0 || size <= MAX_IMPORT_BYTES) { fileTooLarge }
                    context.contentResolver.openInputStream(uri)?.use { it.readLimitedBytes(MAX_IMPORT_BYTES, fileTooLarge) }
                        ?.toString(Charsets.UTF_8) ?: throw IOException(readFailure)
                }.fold(
                    onSuccess = { csv -> app.importCsv(csv) { result -> result.fold(
                        onSuccess = { count -> message = importSuccess.format(count); if (finishOnImport) app.finishOnboarding(); finishOnImport = false },
                        onFailure = { error -> message = "$importFailure: ${error.message ?: importFailure}"; finishOnImport = false }
                    ) } },
                    onFailure = { error -> message = error.message ?: readFailure; finishOnImport = false }
                )
            } else finishOnImport = false
        }
        val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use {
                        it.write(CsvExporter.write(items))
                    } ?: throw IOException(exportFailure)
                }.fold(
                    onSuccess = { message = exportSuccess },
                    onFailure = { error -> message = error.message ?: exportFailure }
                )
            }
        }

        LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); message = null } }

        if (!app.onboardingComplete) {
            OnboardingScreen(
                onCreate = app::finishOnboarding,
                onImport = { finishOnImport = true; importer.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
                onSkip = app::finishOnboarding
            )
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbar) },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding().height(64.dp),
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        NavigationBarItem(destination == Destination.SHELF, { destination = Destination.SHELF }, { Icon(Icons.Outlined.VideoLibrary, null) }, label = { Text(stringResource(R.string.shelf)) })
                        NavigationBarItem(destination == Destination.SWIPE, { destination = Destination.SWIPE }, { Icon(Icons.Outlined.Swipe, null) }, label = { Text(stringResource(R.string.swipe)) })
                        NavigationBarItem(destination == Destination.SETTINGS, { destination = Destination.SETTINGS }, { Icon(Icons.Outlined.Settings, null) }, label = { Text(stringResource(R.string.settings)) })
                    }
                }
            ) { outerPadding ->
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding(bottom = outerPadding.calculateBottomPadding())) {
                    when (destination) {
                        Destination.SHELF -> ShelfScreen(
                            allItems = items, shelfKind = app.shelfKind, view = app.shelfView,
                            onShelfKind = app::updateShelfKind, onView = app::updateShelfView,
                            onImport = { importer.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
                            onUnavailable = { message = it }, onAdd = app::addManual,
                            onFavorite = app::toggleFavorite, onPlayed = app::togglePlayed, onWatchlist = app::toggleWatchlist
                        )
                        Destination.SWIPE -> SwipeScreen(items, app.shelfKind) { destination = Destination.SHELF }
                        Destination.SETTINGS -> SettingsScreen(
                            theme = app.themeMode,
                            dynamic = app.dynamicColor,
                            palette = app.palette,
                            defaultView = app.shelfView,
                            defaultShelf = app.shelfKind,
                            wishlistGhosts = app.showWishlistGhosts,
                            batchScanning = app.batchScanning,
                            haptic = app.hapticConfirmation,
                            onTheme = app::setTheme,
                            onDynamic = app::updateDynamicColor,
                            onPalette = app::updatePalette,
                            onDefaultView = app::updateShelfView,
                            onDefaultShelf = app::updateShelfKind,
                            onWishlistGhosts = app::setWishlistGhosts,
                            onBatchScanning = app::updateBatchScanning,
                            onHaptic = app::updateHapticConfirmation,
                            onImport = { importer.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
                            onExport = { exporter.launch("blushelf_collection.csv") },
                            onLanguage = {
                                val action = if (Build.VERSION.SDK_INT >= 33) Settings.ACTION_APP_LOCALE_SETTINGS else Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                context.startActivity(Intent(action, Uri.parse("package:" + context.packageName)))
                            },
                            onMessage = { message = it }
                        )
                    }
                }
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
