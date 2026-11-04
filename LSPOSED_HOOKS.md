# Hollow Mount — LSPosed / Vector Hook Architecture & Reference Guide

This document is the technical reference for the **Hollow Mount** (`com.hollow.mount`) storage isolation Xposed/LSPosed module.

---

## 1. Module Detection & `AndroidManifest.xml` Contract

| Meta-Data Name | Required Value | Purpose |
| :--- | :--- | :--- |
| `xposedmodule` | `"true"` | Marks the APK as an Xposed module when `assets/xposed_init` is present. |
| `xposedminversion` | `"93"` | Enables the `/data/misc/<uuid>/prefs/<pkg>/` remote preference bridge. |
| `xposedsharedprefs` | `"true"` | Explicit flag to hook `ContextImpl.getSharedPreferencesPath` inside the module app. |
| `xposeddescription` | String | Displayed in the Vector / LSPosed Manager module list. |
| `xposedscope` | `@array/xposed_scope` | Default target packages requested when enabling the module in Vector / LSPosed. |

---

## 2. Per-App Storage Isolation & Hollow Drive

- **File**: `app/src/main/java/com/hollow/mount/isolation/hook/StorageIsolationHook.kt`
- **Modes Supported**:
  - `SHARED_WORKSPACE` ("Hollow Drive"): Shared virtual storage across isolated apps inside `/storage/emulated/0/.HollowMount/Hollow_Drive` (falling back to `/data/user/0/com.hollow.mount/files/hollow_drive` or per-app sandbox if inaccessible).
  - `SANDBOX_REDIRECT` ("Separate App Drive"): Per-app isolated storage inside `/storage/emulated/0/.HollowMount/Drives/<pkg>`.
  - `ENCRYPTED_VAULT` ("Encrypted Separate Drive"): Per-app isolated drive with AES-256 volume sealing support.
  - `FAKE_EMPTY` ("Blank Gallery"): Returns empty media cursors and empty directory listings.
  - `READ_ONLY` ("View Only"): Blocks writes while permitting reads.

| Layer | Hooked Classes & Methods | Purpose |
| :--- | :--- | :--- |
| **Layer 1: Virtual MediaStore & ContentResolver** | `ContentResolver#query`, `ContentProviderClient#query`, `ContentProviderProxy#query`, `ContentResolver#insert`, `ContentResolver#openInputStream`, `openOutputStream`, `openFileDescriptor`, `openAssetFileDescriptor`, `openTypedAssetFileDescriptor`, `loadThumbnail`, `delete`, `ImageDecoder#createSource`, `MediaMetadataRetriever#setDataSource` | Serves `SafeSandboxMatrixCursor` / `SafeEmptyMatrixCursor` populated exclusively from the app's sandbox (`DCIM`, `Pictures`, `Movies`, `Download`), redirects media saves into the sandbox, and blocks reads of real phone media URIs unless present in `userAllowedUris`. |
| **Layer 2: SQLite Camera Roll Cache Guard** | `android.database.sqlite.SQLiteSession#executeForCursorWindow` | Intercepts `SELECT` queries targeting `camera_roll`, `local_media_reference`, or `/storage/emulated/0/dcim` and clears the `CursorWindow` so apps cannot display cached real gallery thumbnails from their local DB. |
| **Layer 3: System Photo & File Picker Interceptor** | `Instrumentation#execStartActivity`, `Activity#startActivityForResult`, `Activity#dispatchActivityResult` | Respects `PickerPolicy.ALLOW` (binds `EXTRA_INITIAL_URI` to sandbox `Pictures`, adds user-picked URIs to `userAllowedUris`, and atomically copies picked files via `.part` -> final file into sandbox) or `PickerPolicy.BLOCK` (cancels picker and delivers `Activity.RESULT_CANCELED`). |
| **Layer 4: `java.io.File` & `BitmapFactory` Redirection** | `File(String)`, `File(String, String)`, `File(File, String)`, `File#list`, `File#listFiles`, `File#exists`, `isDirectory`, `isFile`, `length`, `canRead`, `canWrite`, `BitmapFactory#decodeFile` | Redirects `/storage/emulated/0`, `/sdcard`, `/mnt/sdcard`, and `/storage/self/primary` paths to the app's sandbox (or `/dev/null` when blocking root pollution folders), while preserving whitelisted folders and standing down when a root bind-mount (`MountManager.VOLUME_MARKER`) is active. |
| **Layer 5: Low-Level `libcore.io` & `Environment`** | `libcore.io.IoBridge#open`, `libcore.io.BlockGuardOs#open/stat/lstat/access/opendir`, `android.os.Environment#getExternalStorageDirectory`, `getExternalStoragePublicDirectory` | Rewrites raw POSIX path strings passed to `IoBridge` / `BlockGuardOs` and returns sandbox `File` roots from `Environment` getters. |
