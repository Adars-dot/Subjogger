package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subjogger_prefs")

data class UserPreferences(
    val currencyCode: String = "USD",
    val alertLeadDays: Int = 3,
    val notificationsEnabled: Boolean = true,
    val onDeviceParserEnabled: Boolean = true,
    val bannerDismissed: Boolean = false,
    val hasSeededSampleData: Boolean = false,
    val lastBackupEpochMs: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val ALERT_LEAD_DAYS = intPreferencesKey("alert_lead_days")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ON_DEVICE_PARSER_ENABLED = booleanPreferencesKey("on_device_parser_enabled")
        val BANNER_DISMISSED = booleanPreferencesKey("banner_dismissed")
        val HAS_SEEDED_SAMPLE = booleanPreferencesKey("has_seeded_sample")
        val LAST_BACKUP_EPOCH_MS = longPreferencesKey("last_backup_epoch_ms")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            currencyCode = prefs[Keys.CURRENCY_CODE] ?: "USD",
            alertLeadDays = prefs[Keys.ALERT_LEAD_DAYS] ?: 3,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            onDeviceParserEnabled = prefs[Keys.ON_DEVICE_PARSER_ENABLED] ?: true,
            bannerDismissed = prefs[Keys.BANNER_DISMISSED] ?: false,
            hasSeededSampleData = prefs[Keys.HAS_SEEDED_SAMPLE] ?: false,
            lastBackupEpochMs = prefs[Keys.LAST_BACKUP_EPOCH_MS] ?: 0L
        )
    }

    suspend fun getSnapshot(): UserPreferences = preferencesFlow.first()

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { it[Keys.CURRENCY_CODE] = code }
    }

    suspend fun setAlertLeadDays(days: Int) {
        context.dataStore.edit { it[Keys.ALERT_LEAD_DAYS] = days }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setOnDeviceParserEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ON_DEVICE_PARSER_ENABLED] = enabled }
    }

    suspend fun setBannerDismissed(dismissed: Boolean) {
        context.dataStore.edit { it[Keys.BANNER_DISMISSED] = dismissed }
    }

    suspend fun markSampleSeeded(seeded: Boolean = true) {
        context.dataStore.edit { it[Keys.HAS_SEEDED_SAMPLE] = seeded }
    }

    suspend fun updateLastBackupTimestamp(epochMs: Long = System.currentTimeMillis()) {
        context.dataStore.edit { it[Keys.LAST_BACKUP_EPOCH_MS] = epochMs }
    }
}
