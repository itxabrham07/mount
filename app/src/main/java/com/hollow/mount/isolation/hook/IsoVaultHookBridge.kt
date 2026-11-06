package com.hollow.mount.isolation.hook

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.hollow.mount.isolation.model.IsolationRuleManager

/**
 * Diagnostic bridge for the manager process and the configuration provider.
 * A successful self-hook only proves that the module ran in the manager process; it does not prove
 * that a selected target process was injected or that every storage path there is intercepted.
 */
object IsoVaultHookBridge {

    private const val TAG = "IsoVaultHookBridge"
    const val HOOK_SPEC_VERSION = 3

    /** True only when intercepted in the manager process by StorageIsolationHook. */
    @JvmStatic
    fun isHookActive(): Boolean = false

    /** Non-zero only when intercepted in the manager process by StorageIsolationHook. */
    @JvmStatic
    fun getHookVersion(): Int = 0

    /** Current time only when intercepted in the manager process by StorageIsolationHook. */
    @JvmStatic
    fun getHeartbeatTimestamp(): Long = 0L

    data class FrameworkStatus(
        val isModuleLoadedInManagerProcess: Boolean,
        val isProviderAlive: Boolean,
        val hookVersion: Int,
        val lastHeartbeatMs: Long,
        val summary: String
    )

    /**
     * Checks the module's self-hook, config-file readability, and provider liveness separately.
     * None of these checks proves that a selected target app's storage paths are fully isolated.
     */
    fun checkStatus(context: Context): FrameworkStatus {
        val moduleLoadedInManager = isHookActive()
        var providerAlive = false

        try {
            val providerUri = Uri.parse("content://${IsolationRuleManager.MODULE_PKG}.provider")
            val bundle = context.contentResolver.call(
                providerUri,
                "heartbeat",
                null,
                Bundle()
            )
            providerAlive = bundle?.getBoolean("alive", false) == true
        } catch (e: Exception) {
            Log.d(TAG, "ContentProvider heartbeat check: ${e.message}")
        }

        val summary = when {
            moduleLoadedInManager ->
                "Module loaded in manager process. Target-app hook coverage is unverified."
            providerAlive ->
                "Provider responds only; LSPosed loading and target-app hook coverage are unverified."
            else ->
                "Module not observed in manager process; target-app hook coverage is unknown."
        }

        return FrameworkStatus(
            isModuleLoadedInManagerProcess = moduleLoadedInManager,
            isProviderAlive = providerAlive,
            hookVersion = if (moduleLoadedInManager) getHookVersion() else 0,
            lastHeartbeatMs = if (moduleLoadedInManager) getHeartbeatTimestamp() else 0L,
            summary = summary
        )
    }
}
