package com.prudence.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Small app-wide preferences (SPEC.md §3): last-used category now, display currency lands with #14. */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val lastUsedCategoryId: Flow<Long?> = dataStore.data.map { it[KEY_LAST_USED_CATEGORY_ID] }

    suspend fun setLastUsedCategoryId(id: Long) {
        dataStore.edit { it[KEY_LAST_USED_CATEGORY_ID] = id }
    }

    private companion object {
        val KEY_LAST_USED_CATEGORY_ID = longPreferencesKey("last_used_category_id")
    }
}
