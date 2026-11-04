package com.hollow.mount

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hollow.mount.isolation.model.IsolationMode
import com.hollow.mount.isolation.model.IsolationRuleManager
import com.hollow.mount.isolation.model.PickerPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Hollow Mount", appName)
    }

    @Test
    fun `storage isolation rules publish and persist in shared prefs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        IsolationRuleManager.publishPickerPolicy(PickerPolicy.BLOCK)
        IsolationRuleManager.updateRule(
            packageName = "com.whatsapp",
            mode = IsolationMode.SANDBOX_REDIRECT,
            blockRootPollution = true,
            whitelistedFolders = setOf("Documents", "Download")
        )

        val prefs = IsolationRuleManager.getReadableSharedPrefs(context)
        assertEquals(PickerPolicy.BLOCK.name, prefs.getString(IsolationRuleManager.KEY_PICKER_POLICY, ""))
        assertTrue(prefs.getBoolean("com.whatsapp_configured", false))
        assertTrue(prefs.getBoolean("com.whatsapp_isolated", false))
        assertEquals(IsolationMode.SANDBOX_REDIRECT.name, prefs.getString("com.whatsapp_mode", ""))
    }
}
