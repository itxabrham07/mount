package com.hollow.mount.isolation.model

enum class IsolationMode(
    val title: String,
    val description: String,
    val badge: String
) {
    SHARED_WORKSPACE(
        title = "Hollow Drive (Default)",
        description = "Attempts to redirect selected storage APIs to Hollow Drive. This is not complete isolation; behavior depends on hooks, permissions, and device support.",
        badge = "Hollow Drive"
    ),
    SANDBOX_REDIRECT(
        title = "Separate App Drive",
        description = "Attempts to redirect selected storage paths to this app's drive. Unhooked APIs and device-specific behavior may bypass it.",
        badge = "Separate Drive"
    ),
    ENCRYPTED_VAULT(
        title = "Encrypted Separate Drive",
        description = "A detached volume can be sealed as an AES-GCM archive. The volume is plaintext while attached for app use.",
        badge = "Encrypted Drive"
    ),
    FAKE_EMPTY(
        title = "Blank Gallery",
        description = "Returns empty results on selected hooked gallery paths. Other APIs, permissions, or providers may still expose media.",
        badge = "Blank Gallery"
    ),
    READ_ONLY(
        title = "View Only",
        description = "Attempts to prevent writes on hooked paths; it cannot guarantee that all app write mechanisms are blocked.",
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
        description = "Allows the system picker. Android generally grants access to the items you choose, subject to the picker and URI grant."
    ),
    BLOCK(
        title = "Block",
        description = "Attempts to cancel recognized system-picker launch routes. Unrecognized picker paths may still work."
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
