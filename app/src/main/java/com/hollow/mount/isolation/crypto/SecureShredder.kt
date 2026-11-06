package com.hollow.mount.isolation.crypto

import android.system.Os
import android.system.OsConstants
import com.hollow.mount.isolation.util.HollowResult
import com.hollow.mount.isolation.util.runHollowCatching
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.security.SecureRandom
import java.util.UUID

/**
 * Best-effort overwrite followed by unlink for ordinary files.
 *
 * This is not a secure-erasure guarantee. Overwrite cannot reliably sanitize flash storage because
 * wear levelling, snapshots, remapped blocks, and filesystem behavior are outside this code's
 * control. Prefer deleting ciphertext whose encryption key can be safely destroyed when a stronger
 * deletion property is required. Paths that resolve through symlinks and non-regular file types
 * are rejected to avoid following hostile directory entries or blocking on special files.
 */
object SecureShredder {

    private const val BUFFER_SIZE = 64 * 1024
    private val secureRandom = SecureRandom()

    fun shredFile(
        file: File,
        passes: Int = 2,
        onProgress: ((bytesOverwritten: Long, totalBytes: Long) -> Unit)? = null
    ): HollowResult<Boolean> = runHollowCatching("SecureShredder") {
        require(passes >= 1) { "At least one overwrite pass is required." }
        if (!file.exists() && !isSymlink(file)) return@runHollowCatching false

        val checked = checkedPath(file)
        val type = fileType(checked)
        when (type) {
            OsConstants.S_IFDIR -> {
                val children = checked.listFiles()
                    ?: throw IOException("Could not list directory; nothing further was removed.")
                for (child in children) {
                    val childResult = shredFile(child, passes)
                    if (childResult !is HollowResult.Success || !childResult.data) {
                        throw IOException("Could not remove child '${child.name}'.")
                    }
                }
                if (!checked.delete()) throw IOException("Could not remove directory '${checked.name}'.")
                true
            }
            OsConstants.S_IFREG -> {
                overwriteAndUnlink(checked, passes, onProgress)
                true
            }
            else -> throw IOException("Refusing to overwrite a non-regular file.")
        }
    }

    private fun overwriteAndUnlink(
        file: File,
        passes: Int,
        onProgress: ((bytesOverwritten: Long, totalBytes: Long) -> Unit)?
    ) {
        val totalLength = file.length()
        if (totalLength > 0L) {
            val zeroBuffer = ByteArray(BUFFER_SIZE)
            val randomBuffer = ByteArray(BUFFER_SIZE)
            RandomAccessFile(file, "rws").use { raf ->
                repeat(passes) { pass ->
                    raf.seek(0)
                    var written = 0L
                    while (written < totalLength) {
                        val count = minOf(BUFFER_SIZE.toLong(), totalLength - written).toInt()
                        if (pass == 0) {
                            raf.write(zeroBuffer, 0, count)
                        } else {
                            secureRandom.nextBytes(randomBuffer)
                            raf.write(randomBuffer, 0, count)
                        }
                        written += count
                        onProgress?.invoke((pass * totalLength + written) / passes, totalLength)
                    }
                    raf.fd.sync()
                }
                raf.setLength(0)
                raf.fd.sync()
            }
        }

        // Renaming only obscures the name briefly; it is not represented as a sanitization step.
        val renamed = File(file.parentFile ?: file.absoluteFile.parentFile, ".delete_${UUID.randomUUID()}")
        val removed = if (file.renameTo(renamed)) renamed.delete() else file.delete()
        if (!removed || file.exists() || renamed.exists()) {
            throw IOException("Overwrite completed, but unlink did not complete.")
        }
    }

    private fun checkedPath(file: File): File {
        val absolute = file.absoluteFile
        val canonicalPath = try {
            absolute.canonicalPath
        } catch (e: IOException) {
            throw IOException("Could not resolve the requested path.", e)
        }
        if (canonicalPath != absolute.path) {
            throw IOException("Refusing a path that resolves through a symbolic link or alias.")
        }
        if (isSymlink(absolute)) {
            throw IOException("Refusing to overwrite a symbolic link.")
        }
        return absolute
    }

    private fun fileType(file: File): Int {
        val mode = try {
            Os.lstat(file.absolutePath).st_mode
        } catch (e: Exception) {
            throw IOException("Could not inspect file type.", e)
        }
        return mode and OsConstants.S_IFMT
    }

    private fun isSymlink(file: File): Boolean = try {
        (Os.lstat(file.absolutePath).st_mode and OsConstants.S_IFMT) == OsConstants.S_IFLNK
    } catch (_: Exception) {
        false
    }
}
