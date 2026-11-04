package com.hollow.mount.isolation.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hollow.mount.isolation.data.dao.IsolationDao
import com.hollow.mount.isolation.data.entity.AppIsolationConfig
import com.hollow.mount.isolation.data.entity.VaultFileItem
import com.hollow.mount.isolation.model.IsolationMode

class IsoVaultConverters {
    @TypeConverter
    fun fromIsolationMode(mode: IsolationMode): String = mode.name

    @TypeConverter
    fun toIsolationMode(name: String): IsolationMode = try {
        IsolationMode.valueOf(name)
    } catch (_: Exception) {
        IsolationMode.SHARED_WORKSPACE
    }
}

@Database(
    entities = [
        AppIsolationConfig::class,
        VaultFileItem::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(IsoVaultConverters::class)
abstract class IsoVaultDatabase : RoomDatabase() {
    abstract fun isolationDao(): IsolationDao

    companion object {
        @Volatile
        private var INSTANCE: IsoVaultDatabase? = null

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `access_audit_logs`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_isolation_configs_new` (
                        `packageName` TEXT NOT NULL,
                        `appName` TEXT NOT NULL,
                        `isIsolated` INTEGER NOT NULL,
                        `isolationMode` TEXT NOT NULL,
                        `encryptMediaOnly` INTEGER NOT NULL,
                        `blockRootPollution` INTEGER NOT NULL,
                        `whitelistedFolders` TEXT NOT NULL,
                        `customMountPath` TEXT NOT NULL,
                        `totalFilesRedirected` INTEGER NOT NULL,
                        `totalBytesEncrypted` INTEGER NOT NULL,
                        `lastAccessedTimestamp` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`packageName`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO `app_isolation_configs_new` (
                        `packageName`,
                        `appName`,
                        `isIsolated`,
                        `isolationMode`,
                        `encryptMediaOnly`,
                        `blockRootPollution`,
                        `whitelistedFolders`,
                        `customMountPath`,
                        `totalFilesRedirected`,
                        `totalBytesEncrypted`,
                        `lastAccessedTimestamp`,
                        `createdAt`
                    )
                    SELECT
                        `packageName`,
                        `appName`,
                        `isIsolated`,
                        `isolationMode`,
                        `encryptMediaOnly`,
                        `blockRootPollution`,
                        `whitelistedFolders`,
                        `customMountPath`,
                        `totalFilesRedirected`,
                        `totalBytesEncrypted`,
                        `lastAccessedTimestamp`,
                        `createdAt`
                    FROM `app_isolation_configs`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `app_isolation_configs`")
                db.execSQL("ALTER TABLE `app_isolation_configs_new` RENAME TO `app_isolation_configs`")
            }
        }

        fun getDatabase(context: Context): IsoVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IsoVaultDatabase::class.java,
                    "isovault_database"
                )
                    .addMigrations(MIGRATION_8_9)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
