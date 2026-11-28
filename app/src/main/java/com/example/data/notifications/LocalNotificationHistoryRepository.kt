package com.example.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.DynaApplication
import com.example.domain.model.NotificationHistoryEntry
import com.example.domain.repository.NotificationHistoryRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val Context.dynaHistoryStore: DataStore<Preferences> by
  preferencesDataStore(name = "dyna_notification_history")

class LocalNotificationHistoryRepository(
  private val context: Context,
  private val dayKeyProvider: () -> String = { LocalDate.now().toString() },
  private val bootCountProvider: () -> Int = { readBootCount(context) },
  private val clockMs: () -> Long = { System.currentTimeMillis() },
) : NotificationHistoryRepository {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val _entries = MutableStateFlow<List<NotificationHistoryEntry>>(emptyList())
  override val entries: StateFlow<List<NotificationHistoryEntry>> = _entries.asStateFlow()

  private object Keys {
    val DAY_KEY = stringPreferencesKey("history_day_key")
    val BOOT_COUNT = intPreferencesKey("history_boot_count")
    val ENTRIES_JSON = stringPreferencesKey("history_entries_json")
  }

  init {
    scope.launch { purgeIfDayChangedOrRebooted() }
  }

  override suspend fun recordNotification(
    packageName: String,
    appName: String,
    title: String,
    body: String,
    isChat: Boolean,
  ) {
    purgeIfDayChangedOrRebooted()
    val today = dayKeyProvider()
    val currentBoot = bootCountProvider()
    val newItem =
      NotificationHistoryEntry(
        id = UUID.randomUUID().toString(),
        packageName = packageName,
        appName = appName,
        title = title,
        body = body,
        timestampMs = clockMs(),
        dayKey = today,
        isChat = isChat,
      )

    context.dynaHistoryStore.edit { prefs ->
      val currentList = decodeEntries(prefs[Keys.ENTRIES_JSON], expectedDay = today)
      val updated = (listOf(newItem) + currentList).take(MAX_ENTRIES)
      prefs[Keys.DAY_KEY] = today
      prefs[Keys.BOOT_COUNT] = currentBoot
      prefs[Keys.ENTRIES_JSON] = encodeEntries(updated)
      _entries.value = updated
    }
  }

  override suspend fun deleteEntry(id: String) {
    val today = dayKeyProvider()
    context.dynaHistoryStore.edit { prefs ->
      val current = decodeEntries(prefs[Keys.ENTRIES_JSON], expectedDay = today)
      val updated = current.filterNot { it.id == id }
      prefs[Keys.ENTRIES_JSON] = encodeEntries(updated)
      _entries.value = updated
    }
  }

  override suspend fun clearAll() {
    val today = dayKeyProvider()
    val currentBoot = bootCountProvider()
    context.dynaHistoryStore.edit { prefs ->
      prefs[Keys.DAY_KEY] = today
      prefs[Keys.BOOT_COUNT] = currentBoot
      prefs[Keys.ENTRIES_JSON] = "[]"
      _entries.value = emptyList()
    }
  }

  override suspend fun purgeIfDayChangedOrRebooted() {
    val today = dayKeyProvider()
    val currentBoot = bootCountProvider()
    val prefs = context.dynaHistoryStore.data.first()
    val savedDay = prefs[Keys.DAY_KEY]
    val savedBoot = prefs[Keys.BOOT_COUNT]

    val dayMismatch = savedDay != null && savedDay != today
    val bootMismatch = savedBoot != null && currentBoot >= 0 && savedBoot != currentBoot

    if (dayMismatch || bootMismatch) {
      clearAll()
      return
    }

    val loaded = decodeEntries(prefs[Keys.ENTRIES_JSON], expectedDay = today)
    if (savedDay == null || (savedBoot == null && currentBoot >= 0)) {
      context.dynaHistoryStore.edit { editPrefs ->
        editPrefs[Keys.DAY_KEY] = today
        editPrefs[Keys.BOOT_COUNT] = currentBoot
      }
    }
    _entries.value = loaded
  }

  override suspend fun onDeviceShutdownOrReboot() {
    clearAll()
  }

  private fun encodeEntries(list: List<NotificationHistoryEntry>): String {
    val arr = JSONArray()
    list.forEach { item ->
      arr.put(
        JSONObject().apply {
          put("id", item.id)
          put("packageName", item.packageName)
          put("appName", item.appName)
          put("title", item.title)
          put("body", item.body)
          put("timestampMs", item.timestampMs)
          put("dayKey", item.dayKey)
          put("isChat", item.isChat)
        }
      )
    }
    return arr.toString()
  }

  private fun decodeEntries(rawJson: String?, expectedDay: String): List<NotificationHistoryEntry> {
    if (rawJson.isNullOrBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(rawJson)
        buildList {
          for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val dayKey = obj.optString("dayKey")
            if (dayKey != expectedDay) continue
            add(
              NotificationHistoryEntry(
                id = obj.optString("id"),
                packageName = obj.optString("packageName"),
                appName = obj.optString("appName"),
                title = obj.optString("title"),
                body = obj.optString("body"),
                timestampMs = obj.optLong("timestampMs"),
                dayKey = dayKey,
                isChat = obj.optBoolean("isChat", false),
              )
            )
          }
        }
      }
      .getOrDefault(emptyList())
  }

  companion object {
    private const val MAX_ENTRIES = 120

    private fun readBootCount(context: Context): Int =
      runCatching {
          Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
        }
        .getOrDefault(-1)
  }
}

class NotificationHistoryLifecycleReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    if (action == Intent.ACTION_SHUTDOWN || action == Intent.ACTION_BOOT_COMPLETED) {
      val app = context.applicationContext as? DynaApplication ?: return
      val pendingResult = goAsync()
      CoroutineScope(Dispatchers.IO).launch {
        runCatching { app.container.notificationHistoryRepository.onDeviceShutdownOrReboot() }
        pendingResult.finish()
      }
    }
  }
}
