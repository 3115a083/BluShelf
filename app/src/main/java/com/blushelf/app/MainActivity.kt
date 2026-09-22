package com.blushelf.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blushelf.app.ui.BluShelfTheme
import com.blushelf.app.ui.OnboardingScreen
import com.blushelf.app.ui.SettingsScreen
import com.blushelf.app.ui.ShelfScreen
import com.blushelf.app.ui.SwipeScreen
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
                    NavigationBar {
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
                            onShelfKind = app::setShelfKind, onView = app::setShelfView,
                            onImport = { importer.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
                            onUnavailable = { message = it }, onAdd = app::addManual,
                            onFavorite = app::toggleFavorite, onPlayed = app::togglePlayed, onWatchlist = app::toggleWatchlist
                        )
                        Destination.SWIPE -> SwipeScreen(items, app.shelfKind) { destination = Destination.SHELF }
                        Destination.SETTINGS -> SettingsScreen(
                            app.themeMode, app.dynamicColor, app.palette, app.shelfView, app.showWishlistGhosts,
                            app.batchScanning, app.hapticConfirmation, app::setTheme, app::setDynamicColor,
                            app::setPalette, app::setShelfView, app::setWishlistGhosts, app::setBatchScanning,
                            app::setHapticConfirmation
                        ) { message = it }
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
