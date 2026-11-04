package com.hollow.mount.isolation.model

enum class IsolationMode(
    val title: String,
    val description: String,
    val badge: String
) {
    SHARED_WORKSPACE(
        title = "Hollow Drive (Default)",
        description = "Connects this app to Hollow Drive — the default shared virtual storage and gallery used across isolated apps.",
        badge = "Hollow Drive"
    ),
    SANDBOX_REDIRECT(
        title = "Separate App Drive",
        description = "Assigns this app its own Separate Drive that appears below Hollow Drive in Files like an SD card.",
        badge = "Separate Drive"
    ),
    ENCRYPTED_VAULT(
        title = "Encrypted Separate Drive",
        description = "Assigns this app an AES-256 encrypted Separate Drive below Hollow Drive in Files.",
        badge = "Encrypted Drive"
    ),
    FAKE_EMPTY(
        title = "Blank Gallery",
        description = "Shows an empty gallery and folder list so your personal media stays unseen.",
        badge = "Blank Gallery"
    ),
    READ_ONLY(
        title = "View Only",
        description = "Stops the app from creating extra clutter or hidden folders on your phone.",
        badge = "View Only"
    )
}

enum class MountEngineStatus {
    ACTIVE_LSPOSED,
    STANDALONE_USERSPACE,
    ROOT_FUSE_ATTACHED,
    INACTIVE
}

/** What a hooked app may do when it opens the system photo / file picker. */
enum class PickerPolicy(val title: String, val description: String) {
    ALLOW(
        title = "Allow",
        description = "The system picker opens normally and shows your whole phone. The app only receives what you choose."
    ),
    BLOCK(
        title = "Block",
        description = "The system picker is cancelled for isolated apps. They can only use their own in-app gallery, which is isolated."
    )
}

/**
 * Selectable launcher icon styles for Hollow Mount.
 * Supports Android 12+ dynamic wallpaper-reactive colors and Android 13+ monochrome themed icons.
 */
enum class HollowIconTheme(
    val label: String,
    val subtitle: String,
    val aliasClassName: String?
) {
    DYNAMIC_MATERIAL_YOU(
        label = "Dynamic",
        subtitle = "Adapts to wallpaper & Material You",
        aliasClassName = null // Uses default MainActivity with drawable-v31 dynamic vectors + monochrome
    ),
    CYBER_ECLIPSE(
        label = "Cyber Cyan",
        subtitle = "Obsidian & electric cyan portal",
        aliasClassName = "com.hollow.mount.MainActivityCyberAlias"
    ),
    CRIMSON_VOID(
        label = "Crimson",
        subtitle = "Carbon black & crimson ring",
        aliasClassName = "com.hollow.mount.MainActivityCrimsonAlias"
    ),
    OBSIDIAN_MINIMAL(
        label = "Minimal",
        subtitle = "Stealth monochrome titanium",
        aliasClassName = "com.hollow.mount.MainActivityMinimalAlias"
    )
}
