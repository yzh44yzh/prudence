package com.prudence.app.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SettingsRepositoryTest {

    @Test
    fun `last-used category id round-trips through DataStore`() = runTest {
        val dir = Files.createTempDirectory("settings-test").toFile()
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(dir, "settings.preferences_pb")
        }
        val settings = SettingsRepository(dataStore)

        assertNull(settings.lastUsedCategoryId.first())

        settings.setLastUsedCategoryId(42L)
        assertEquals(42L, settings.lastUsedCategoryId.first())
    }
}
