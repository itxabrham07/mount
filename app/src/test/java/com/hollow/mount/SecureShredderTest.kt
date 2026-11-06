package com.hollow.mount

import com.hollow.mount.isolation.crypto.SecureShredder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecureShredderTest {
    @Test
    fun directorySymlinkIsRejectedWithoutChangingTargetContents() {
        val root = Files.createTempDirectory("hollow-shred-test").toFile()
        try {
            val target = Files.createDirectory(root.toPath().resolve("target")).toFile()
            val protectedFile = target.resolve("keep.txt").apply { writeText("must remain unchanged") }
            val link = root.toPath().resolve("linked-directory")
            Files.createSymbolicLink(link, target.toPath())

            val result = SecureShredder.shredFile(link.toFile())

            assertTrue("Symlink deletion must fail closed", result.isFailure)
            assertTrue("The symlink must remain intact", Files.isSymbolicLink(link))
            assertEquals("must remain unchanged", protectedFile.readText())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun overwritePassCountMustBePositive() {
        val root = Files.createTempDirectory("hollow-shred-test").toFile()
        try {
            val file = root.resolve("sample.txt").apply { writeText("sample") }
            val result = SecureShredder.shredFile(file, passes = 0)
            assertTrue(result.isFailure)
            assertEquals("sample", file.readText())
        } finally {
            root.deleteRecursively()
        }
    }
}
