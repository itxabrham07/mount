package com.hollow.mount.isolation.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hollow.mount.IsoVaultApp
import com.hollow.mount.isolation.feature.apps.AppIsolationViewModel
import com.hollow.mount.isolation.feature.files.FileManagerViewModel
import com.hollow.mount.isolation.feature.gallery.GalleryViewModel
import com.hollow.mount.isolation.feature.settings.SettingsViewModel
import com.hollow.mount.isolation.feature.vault.VaultViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = isoVaultApplication()
            AppIsolationViewModel(
                app = app,
                configRepository = app.configRepository,
                vaultRepository = app.vaultRepository,
                settingsRepository = app.settingsRepository
            )
        }
        initializer {
            val app = isoVaultApplication()
            VaultViewModel(
                vaultRepository = app.vaultRepository,
                configRepository = app.configRepository,
                settingsRepository = app.settingsRepository
            )
        }
        initializer {
            val app = isoVaultApplication()
            SettingsViewModel(
                app = app,
                settingsRepository = app.settingsRepository,
                configRepository = app.configRepository
            )
        }
        initializer {
            FileManagerViewModel(isoVaultApplication())
        }
        initializer {
            GalleryViewModel(isoVaultApplication())
        }
    }
}

fun CreationExtras.isoVaultApplication(): IsoVaultApp =
    (this[APPLICATION_KEY] as IsoVaultApp)
