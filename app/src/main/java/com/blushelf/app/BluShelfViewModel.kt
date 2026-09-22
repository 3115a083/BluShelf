package com.blushelf.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blushelf.app.data.BluShelfDatabase
import com.blushelf.app.data.CsvImporter
import com.blushelf.app.data.MediaItem
import com.blushelf.app.data.MediaKind
import com.blushelf.app.data.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ColorPalette { OCEAN, INDIGO, TEAL }
enum class ShelfKind { VIDEO, AUDIO }
enum class ShelfView { VIRTUAL, LIST, DETAILED }

class BluShelfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaRepository(BluShelfDatabase.get(application).mediaDao())
    private val preferences = application.getSharedPreferences("blushelf_preferences", 0)

    val items = repository.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var onboardingComplete by mutableStateOf(preferences.getBoolean("onboarding_complete", false)); private set
    var themeMode by mutableStateOf(ThemeMode.valueOf(preferences.getString("theme", ThemeMode.SYSTEM.name)!!)); private set
    var dynamicColor by mutableStateOf(preferences.getBoolean("dynamic_color", true)); private set
    var palette by mutableStateOf(ColorPalette.valueOf(preferences.getString("palette", ColorPalette.OCEAN.name)!!)); private set
    var shelfKind by mutableStateOf(ShelfKind.valueOf(preferences.getString("shelf_kind", ShelfKind.VIDEO.name)!!)); private set
    var shelfView by mutableStateOf(ShelfView.valueOf(preferences.getString("shelf_view", ShelfView.VIRTUAL.name)!!)); private set
    var showWishlistGhosts by mutableStateOf(preferences.getBoolean("wishlist_ghosts", true)); private set
    var batchScanning by mutableStateOf(preferences.getBoolean("batch_scanning", true)); private set
    var hapticConfirmation by mutableStateOf(preferences.getBoolean("haptic", true)); private set

    fun finishOnboarding() { onboardingComplete = true; preferences.edit().putBoolean("onboarding_complete", true).apply() }
    fun setTheme(value: ThemeMode) { themeMode = value; preferences.edit().putString("theme", value.name).apply() }
    fun updateDynamicColor(value: Boolean) { dynamicColor = value; preferences.edit().putBoolean("dynamic_color", value).apply() }
    fun updatePalette(value: ColorPalette) { palette = value; preferences.edit().putString("palette", value.name).apply() }
    fun updateShelfKind(value: ShelfKind) { shelfKind = value; preferences.edit().putString("shelf_kind", value.name).apply() }
    fun updateShelfView(value: ShelfView) { shelfView = value; preferences.edit().putString("shelf_view", value.name).apply() }
    fun setWishlistGhosts(value: Boolean) { showWishlistGhosts = value; preferences.edit().putBoolean("wishlist_ghosts", value).apply() }
    fun updateBatchScanning(value: Boolean) { batchScanning = value; preferences.edit().putBoolean("batch_scanning", value).apply() }
    fun updateHapticConfirmation(value: Boolean) { hapticConfirmation = value; preferences.edit().putBoolean("haptic", value).apply() }

    fun importCsv(text: String, result: (Result<Int>) -> Unit) {
        CsvImporter.parse(text).fold(
            onSuccess = { imported -> viewModelScope.launch { runCatching { repository.replaceAll(imported); imported.size }.also(result) } },
            onFailure = { result(Result.failure(it)) }
        )
    }

    fun addManual(title: String, kind: MediaKind, format: String, year: Int?) = viewModelScope.launch {
        repository.add(MediaItem(id = UUID.randomUUID().toString(), title = title, kind = kind, format = format, year = year))
    }

    fun toggleFavorite(item: MediaItem) = viewModelScope.launch { repository.setFavorite(item.id, !item.favorite) }
    fun togglePlayed(item: MediaItem) = viewModelScope.launch { repository.setPlayed(item.id, !item.played) }
    fun toggleWatchlist(item: MediaItem) = viewModelScope.launch { repository.setWatchlist(item.id, !item.inWatchlist) }
}
