package com.hollow.mount.isolation.data.repository

import android.content.Context
import com.hollow.mount.isolation.data.dao.IsolationDao
import com.hollow.mount.isolation.data.entity.AppIsolationConfig
import com.hollow.mount.isolation.model.IsolationMode
import com.hollow.mount.isolation.model.IsolationRuleManager
import com.hollow.mount.isolation.pkg.AppPackageRepository
import com.hollow.mount.isolation.pkg.InstalledAppInfo
import com.hollow.mount.isolation.provider.IsoVaultConfigProvider
import com.hollow.mount.isolation.util.runHollowCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Central repository for per-app storage isolation configurations.
 */
class IsolationConfigRepository(
    val context: Context,
    private val dao: IsolationDao,
    private val pkgRepo: AppPackageRepository,
    private val appScope: CoroutineScope
) {
    val configs: StateFlow<List<AppIsolationConfig>> = dao.getAllConfigs()
        .stateIn(appScope, SharingStarted.Eagerly, emptyList())

    val isolatedCount: Flow<Int> = dao.getIsolatedAppsCount()

    private val _rawInstalledApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val rawInstalledApps: StateFlow<List<InstalledAppInfo>> = _rawInstalledApps.asStateFlow()

    val allInstalledApps: StateFlow<List<InstalledAppInfo>> = combine(
        _rawInstalledApps,
        configs
    ) { apps, cfgList ->
        val cfgMap = cfgList.associateBy { it.packageName }
        apps.map { app ->
            val cfg = cfgMap[app.packageName]
            app.copy(
                isIsolated = cfg?.isIsolated ?: false,
                isolationMode = cfg?.isolationMode ?: IsolationMode.SHARED_WORKSPACE
            )
        }
    }.stateIn(appScope, SharingStarted.Eagerly, emptyList())

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        appScope.launch(Dispatchers.IO) {
            runHollowCatching("IsolationConfigRepository") {
                val apps = pkgRepo.getInstalledApps(configs.value, includeSystemApps = true)
                _rawInstalledApps.value = apps
            }
        }
    }

    suspend fun getAllConfigsSync(): List<AppIsolationConfig> =
        withContext(Dispatchers.IO) {
            runHollowCatching("IsolationConfigRepository") {
                dao.getAllConfigsSync()
            }.getOrNull() ?: emptyList()
        }

    suspend fun getConfigForPackage(packageName: String): AppIsolationConfig? =
        withContext(Dispatchers.IO) {
            runHollowCatching("IsolationConfigRepository") {
                dao.getConfigForPackage(packageName)
            }.getOrNull()
        }

    suspend fun insertOrUpdateConfig(config: AppIsolationConfig) {
        withContext(Dispatchers.IO) {
            runHollowCatching("IsolationConfigRepository") {
                dao.insertOrUpdateConfig(config)
            }
        }
    }

    fun notifyConfigChanged() {
        runHollowCatching("IsolationConfigRepository") {
            context.contentResolver.notifyChange(
                IsoVaultConfigProvider.CONTENT_URI,
                null
            )
        }
    }

    fun whitelistSet(config: AppIsolationConfig): Set<String> =
        config.whitelistedFolders.split(',').map { it.trim() }.filter { it.isNotBlank() }.toSet()

    fun publishEnabledRule(config: AppIsolationConfig) {
        IsolationRuleManager.updateRule(
            packageName = config.packageName,
            mode = config.isolationMode,
            blockRootPollution = config.blockRootPollution,
            whitelistedFolders = whitelistSet(config)
        )
    }

    fun disableRule(config: AppIsolationConfig) {
        IsolationRuleManager.disableRule(
            packageName = config.packageName,
            mode = config.isolationMode,
            blockRootPollution = config.blockRootPollution,
            whitelistedFolders = whitelistSet(config)
        )
    }
}
