package com.hollow.mount.isolation.pkg

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.hollow.mount.isolation.data.entity.AppIsolationConfig
import com.hollow.mount.isolation.model.IsolationMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val targetSdkVersion: Int,
    val isIsolated: Boolean = false,
    val isolationMode: IsolationMode = IsolationMode.SHARED_WORKSPACE,
    val hasStoragePermission: Boolean = true,
    val icon: Drawable? = null
)

class AppPackageRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    private fun buildInstalledAppInfo(
        pkg: String,
        appName: String,
        isSystem: Boolean,
        targetSdk: Int,
        icon: Drawable?,
        config: AppIsolationConfig?
    ): InstalledAppInfo {
        return InstalledAppInfo(
            packageName = pkg,
            appName = appName,
            isSystemApp = isSystem,
            targetSdkVersion = targetSdk,
            isIsolated = config?.isIsolated ?: false,
            isolationMode = config?.isolationMode ?: IsolationMode.SHARED_WORKSPACE,
            hasStoragePermission = true,
            icon = icon
        )
    }

    suspend fun getInstalledApps(
        configs: List<AppIsolationConfig>,
        includeSystemApps: Boolean = false
    ): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val configMap = configs.associateBy { it.packageName }
        val resultMap = mutableMapOf<String, InstalledAppInfo>()

        // 1. Query launcher apps (user-visible installed apps like Snapchat, Chrome, WhatsApp, etc.)
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in resolveInfos) {
                val appInfo = resolveInfo.activityInfo?.applicationInfo ?: continue
                val pkg = appInfo.packageName
                if (pkg == context.packageName) continue

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isBrowser = pkg == "com.android.chrome" || pkg.contains("browser") || pkg.contains("webview")
                if (!includeSystemApps && isSystem && !isBrowser) continue

                val appName = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkg
                }

                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (_: Exception) {
                    ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
                }

                resultMap[pkg] = buildInstalledAppInfo(
                    pkg = pkg,
                    appName = appName,
                    isSystem = isSystem,
                    targetSdk = appInfo.targetSdkVersion,
                    icon = icon,
                    config = configMap[pkg]
                )
            }
        } catch (_: Exception) {}

        // 2. Query all installed packages
        try {
            val installedPackages = packageManager.getInstalledPackages(0)
            for (pkgInfo in installedPackages) {
                val appInfo = pkgInfo.applicationInfo ?: continue
                val pkg = pkgInfo.packageName
                if (pkg == context.packageName || resultMap.containsKey(pkg)) continue

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isBrowser = pkg == "com.android.chrome" || pkg.contains("browser") || pkg.contains("webview")
                if (!includeSystemApps && isSystem && !isBrowser) continue

                val appName = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkg
                }

                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (_: Exception) {
                    ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
                }

                resultMap[pkg] = buildInstalledAppInfo(
                    pkg = pkg,
                    appName = appName,
                    isSystem = isSystem,
                    targetSdk = appInfo.targetSdkVersion,
                    icon = icon,
                    config = configMap[pkg]
                )
            }
        } catch (_: Exception) {}

        // Always ensure any configured app is present in the list
        configs.forEach { cfg ->
            if (!resultMap.containsKey(cfg.packageName)) {
                resultMap[cfg.packageName] = buildInstalledAppInfo(
                    pkg = cfg.packageName,
                    appName = cfg.appName,
                    isSystem = false,
                    targetSdk = 35,
                    icon = ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon),
                    config = cfg
                )
            }
        }

        resultMap.values.sortedWith(
            compareByDescending<InstalledAppInfo> {
                it.isIsolated
            }.thenBy { it.appName.lowercase() }
        )
    }
}
