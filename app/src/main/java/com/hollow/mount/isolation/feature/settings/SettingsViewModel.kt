package com.hollow.mount.isolation.feature.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hollow.mount.isolation.data.repository.IsolationConfigRepository
import com.hollow.mount.isolation.data.repository.SettingsRepository
import com.hollow.mount.isolation.model.AppSettings
import com.hollow.mount.isolation.model.HollowIconTheme
import com.hollow.mount.isolation.model.PickerPolicy
import com.hollow.mount.isolation.mount.MountEntry
import com.hollow.mount.isolation.mount.MountManager
import com.hollow.mount.isolation.pkg.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val app: Application,
    private val settingsRepository: SettingsRepository,
    private val configRepository: IsolationConfigRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
    val moduleLoadedInManagerProcess: StateFlow<Boolean> = settingsRepository.moduleLoadedInManagerProcess
    val rootStatus: StateFlow<String> = settingsRepository.rootStatus
    val terminalLogs: StateFlow<List<String>> = settingsRepository.terminalLogs
    val systemMounts: StateFlow<List<MountEntry>> = settingsRepository.systemMounts
    val selinuxStatus: StateFlow<String> = settingsRepository.selinuxStatus
    val statusMessage: StateFlow<String?> = settingsRepository.statusMessage
    val allInstalledApps: StateFlow<List<InstalledAppInfo>> = configRepository.allInstalledApps

    fun clearStatusMessage() {
        settingsRepository.clearStatusMessage()
    }

    fun setDarkTheme(dark: Boolean) {
        settingsRepository.updateSettings { it.copy(darkTheme = dark) }
    }

    fun setDynamicColor(dynamic: Boolean) {
        settingsRepository.updateSettings { it.copy(dynamicColor = dynamic) }
    }

    fun setRootVolumesEnabled(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(rootVolumesEnabled = enabled) }
        if (enabled) checkRoot()
    }

    fun setEncryptVolumesAtRest(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(encryptVolumesAtRest = enabled) }
    }

    fun setHollowIconTheme(theme: HollowIconTheme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateSettings { it.copy(hollowIconTheme = theme) }
            settingsRepository.applyLauncherIconTheme(theme)
            settingsRepository.postStatusMessage("Hollow Mount icon updated to ${theme.label}")
        }
    }

    fun setPickerPolicy(policy: PickerPolicy) {
        settingsRepository.updateSettings { it.copy(pickerPolicy = policy) }
        settingsRepository.postStatusMessage("System picker policy set to ${policy.title}")
    }

    fun checkRoot() {
        viewModelScope.launch {
            settingsRepository.checkRoot()
        }
    }

    fun runRootCheck() {
        checkRoot()
    }

    fun generateBootScript(): String {
        val packages = configRepository.configs.value.filter { it.isIsolated }.map { it.packageName }
        return MountManager.generateBootService(app, packages)
    }
}
