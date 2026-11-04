package com.hollow.mount.isolation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hollow.mount.isolation.data.entity.AppIsolationConfig
import com.hollow.mount.isolation.data.entity.VaultFileItem
import kotlinx.coroutines.flow.Flow

@Dao
interface IsolationDao {
    // Configs
    @Query("SELECT * FROM app_isolation_configs ORDER BY isIsolated DESC, appName ASC")
    fun getAllConfigs(): Flow<List<AppIsolationConfig>>

    @Query("SELECT * FROM app_isolation_configs ORDER BY isIsolated DESC, appName ASC")
    suspend fun getAllConfigsSync(): List<AppIsolationConfig>

    @Query("SELECT * FROM app_isolation_configs WHERE packageName = :pkg LIMIT 1")
    suspend fun getConfigForPackage(pkg: String): AppIsolationConfig?

    @Query("SELECT * FROM app_isolation_configs WHERE isIsolated = 1")
    fun getIsolatedConfigs(): Flow<List<AppIsolationConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AppIsolationConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<AppIsolationConfig>)

    @Update
    suspend fun updateConfig(config: AppIsolationConfig)

    @Query("UPDATE app_isolation_configs SET isIsolated = :isolated WHERE packageName = :pkg")
    suspend fun setIsolationStatus(pkg: String, isolated: Boolean)

    @Query("UPDATE app_isolation_configs SET totalFilesRedirected = totalFilesRedirected + 1, lastAccessedTimestamp = :now WHERE packageName = :pkg")
    suspend fun incrementRedirectCount(pkg: String, now: Long)

    @Query("UPDATE app_isolation_configs SET totalBytesEncrypted = totalBytesEncrypted + :bytes WHERE packageName = :pkg")
    suspend fun addEncryptedBytes(pkg: String, bytes: Long)

    // Vault Files
    @Query("SELECT * FROM vault_files ORDER BY lastModified DESC")
    fun getAllVaultFiles(): Flow<List<VaultFileItem>>

    @Query("SELECT * FROM vault_files ORDER BY lastModified DESC")
    suspend fun getAllVaultFilesSync(): List<VaultFileItem>

    @Query("SELECT * FROM vault_files WHERE packageName = :pkg ORDER BY lastModified DESC")
    fun getVaultFilesForPackage(pkg: String): Flow<List<VaultFileItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultFile(item: VaultFileItem): Long

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteVaultFile(id: Long)

    @Query("DELETE FROM vault_files WHERE packageName = :pkg")
    suspend fun deleteVaultFilesForPackage(pkg: String)

    // Statistics
    @Query("SELECT COUNT(*) FROM app_isolation_configs WHERE isIsolated = 1")
    fun getIsolatedAppsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(encryptedSizeBytes), 0) FROM vault_files WHERE isEncrypted = 1")
    fun getTotalEncryptedBytes(): Flow<Long>
}
