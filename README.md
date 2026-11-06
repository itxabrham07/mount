# Hollow Mount

**Hollow Mount** is an experimental Android storage-mediation app. Its LSPosed hooks and optional root mounts attempt to redirect selected storage paths and APIs; they do not provide a kernel-enforced guarantee that every app access path is isolated.

Actual behavior depends on Android version, app permissions, device/framework changes, module scope, and the APIs an app uses. Native file access, content providers, and user-granted picker URIs may not follow the same paths as the hooks.

---

## Why Hollow Mount?

Many apps demand broad storage or media permissions just to function, exposing thousands of personal photos, downloads, and private documents—while also cluttering your phone with stray folders and hidden tracking files.

**Hollow Mount** solves this by letting you decide **per app** how storage works:
- Keep personal photos and documents invisible to social, messaging, or utility apps.
- Share a clean, dedicated workspace across isolated apps when you want them to exchange files safely.
- Give sensitive apps their own private or encrypted virtual drive.
- Stop apps from littering your main storage with unwanted folders and cached junk.

---

## Core Features

### 1. Per-App Storage Isolation Modes
Choose the exact storage behavior for every isolated app:

- **Hollow Drive (Default Shared Workspace)**  
  Connects the app to **Hollow Drive**—a clean, shared virtual storage and gallery used across your isolated apps. Apps in this mode can only see files placed inside Hollow Drive, never your personal phone storage.
- **Separate App Drive**  
  Assigns the app its own dedicated virtual drive (similar to a private SD card). Files created or saved by the app stay strictly inside its own drive and cannot be seen by any other app.
- **Encrypted Separate Drive**  
  A volume can be sealed into an AES-GCM encrypted archive while detached. The mounted volume is plaintext so the target app can use it; sealing is not continuous encryption, and the Android Keystore key is app-data-bound.
- **Blank Gallery**  
  Presents an empty photo gallery and folder list to the app so your personal media stays completely unseen while the app continues to work normally.
- **View Only**  
  Allows viewing while preventing the app from writing new files, hidden trackers, or extra folder clutter onto your storage.

---

### 2. Built-in Isolated File & Media Manager
Manage all of your isolated storage spaces directly from one unified **Files** hub:

- **Hollow Drive & Separate App Drives:** Browse your shared Hollow Drive alongside individual app drives as distinct storage volumes.
- **Smart Media Categories:** Quickly filter and preview **Images, Videos, Audio, Documents, Downloads, and Archives** inside your isolated drives.
- **Isolated Gallery Viewer:** Inspect photos and media stored across your virtual drives without mixing them into your personal system gallery.
- **Encrypted Vault & best-effort deletion:** Encrypt vault files with AES-GCM. Overwrite-and-unlink is best effort only; Android flash storage, wear levelling, snapshots, and remapped blocks mean this app cannot guarantee forensic erasure.
- **Full File Operations:** Create folders, import files from your phone into an isolated drive, copy, move, rename, compress/extract archives, and share safely.

---

### 3. System Photo & File Picker Control
Control what happens when an isolated app tries to open the Android system file or photo picker:

- **Allow System Picker:** Let the system picker open normally so you can cherry-pick a single photo or file from your phone to share with the app on demand.
- **Block System Picker:** Prevent isolated apps from opening the system-wide picker altogether, forcing them to rely strictly on their isolated gallery and drive.

---

### 4. Private Root Volume Management (Optional)
For advanced users with root access, Hollow Mount can attach and detach dedicated virtual storage volumes on demand:
- **On-Demand Attach & Detach:** Mount private storage volumes only when needed and detach them when done.
- **Automatic Rest Sealing:** Automatically seal and encrypt detached volumes at rest for maximum physical and digital security.
- **Live Mount Diagnostics:** View active volume status, storage telemetry, and isolation health in real time.

---

### 5. Personalization & Stealth Themes
Tailor Hollow Mount's look and home screen presence to match your device:
- **Material 3 Pro Dark & Light Themes** with optional **Material You Dynamic Wallpaper Colors**.
- **Selectable Launcher Icons:** Switch seamlessly between **Dynamic Material You**, **Cyber Cyan**, **Crimson**, and **Minimal Stealth** home screen icons.

---

## Key Benefits at a Glance

- **Storage mediation (experimental):** The app attempts to redirect selected storage paths and APIs; this is not complete isolation and can be bypassed or limited by unhooked paths, permissions, URI grants, or platform changes.
- **Clean Phone Storage:** Eliminate random app folders and leftover junk from your main storage.
- **Granular Per-App Control:** Mix and match shared drives, private drives, encrypted drives, or blank galleries for each app.
- **Effortless File Transfer:** Move files between your real storage, Hollow Drive, and individual app drives anytime using the built-in File Manager.
