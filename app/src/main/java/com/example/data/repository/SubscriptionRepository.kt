package com.example.data.repository

import com.example.data.local.SampleDataProvider
import com.example.data.local.SubscriptionDao
import com.example.data.local.SubscriptionEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.SubscriptionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionRepository(
    private val dao: SubscriptionDao,
    private val prefsRepo: UserPreferencesRepository
) {
    val subscriptionsFlow: Flow<List<SubscriptionItem>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    val preferencesFlow: Flow<UserPreferences> = prefsRepo.preferencesFlow

    suspend fun seedSampleDataIfFirstLaunch() {
        val prefs = prefsRepo.getSnapshot()
        if (!prefs.hasSeededSampleData && dao.getCount() == 0) {
            val samples = SampleDataProvider.createSampleLedger(prefs.currencyCode)
            dao.insertAll(samples.map { SubscriptionEntity.fromDomain(it) })
            prefsRepo.markSampleSeeded(true)
        }
    }

    suspend fun resetSampleData() {
        val prefs = prefsRepo.getSnapshot()
        dao.deleteAll()
        val samples = SampleDataProvider.createSampleLedger(prefs.currencyCode)
        dao.insertAll(samples.map { SubscriptionEntity.fromDomain(it) })
        prefsRepo.markSampleSeeded(true)
    }

    suspend fun clearAllSubscriptions() {
        dao.deleteAll()
        prefsRepo.markSampleSeeded(true)
    }

    suspend fun getAllOnce(): List<SubscriptionItem> =
        dao.getAllOnce().map { it.toDomain() }

    suspend fun addSubscription(item: SubscriptionItem): Long {
        return dao.insert(SubscriptionEntity.fromDomain(item.copy(updatedAt = System.currentTimeMillis())))
    }

    suspend fun addAll(items: List<SubscriptionItem>) {
        val now = System.currentTimeMillis()
        dao.insertAll(items.map { SubscriptionEntity.fromDomain(it.copy(updatedAt = now)) })
    }

    suspend fun updateSubscription(item: SubscriptionItem) {
        dao.update(SubscriptionEntity.fromDomain(item.copy(updatedAt = System.currentTimeMillis())))
    }

    suspend fun deleteSubscription(id: Long) {
        dao.deleteById(id)
    }

    suspend fun replaceAllFromBackup(items: List<SubscriptionItem>) {
        dao.deleteAll()
        dao.insertAll(items.map { SubscriptionEntity.fromDomain(it.copy(id = 0)) })
        prefsRepo.markSampleSeeded(true)
    }

    suspend fun setCurrencyCode(code: String) = prefsRepo.setCurrencyCode(code)
    suspend fun setAlertLeadDays(days: Int) = prefsRepo.setAlertLeadDays(days)
    suspend fun setNotificationsEnabled(enabled: Boolean) = prefsRepo.setNotificationsEnabled(enabled)
    suspend fun setOnDeviceParserEnabled(enabled: Boolean) = prefsRepo.setOnDeviceParserEnabled(enabled)
    suspend fun setBannerDismissed(dismissed: Boolean) = prefsRepo.setBannerDismissed(dismissed)
    suspend fun updateLastBackupTimestamp() = prefsRepo.updateLastBackupTimestamp()
}
