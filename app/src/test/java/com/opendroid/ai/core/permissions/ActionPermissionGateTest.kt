package com.opendroid.ai.core.permissions

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActionPermissionGateTest {

    private val baseContext: Context = ApplicationProvider.getApplicationContext()

    private fun testContext(vararg grantedPermissions: String): Context {
        val grantedSet = grantedPermissions.toSet()
        return object : ContextWrapper(baseContext) {
            override fun checkSelfPermission(permission: String): Int {
                return if (permission in grantedSet) PackageManager.PERMISSION_GRANTED
                else PackageManager.PERMISSION_DENIED
            }

            override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
                return checkSelfPermission(permission)
            }
        }
    }

    @Test
    fun `actions without runtime permissions pass automatically`() {
        val ctx = testContext()
        val result = ActionPermissionGate.checkPermissions(ctx, "OPEN_APP")
        assertTrue(result.isGranted)
        assertTrue(result.missingPermissions.isEmpty())
    }

    @Test
    fun `SMS action fails when SEND_SMS permission is not granted`() {
        val ctx = testContext()
        val result = ActionPermissionGate.checkPermissions(ctx, "SEND_SMS")
        assertFalse(result.isGranted)
        assertTrue(result.missingPermissions.contains(Manifest.permission.SEND_SMS))
        assertTrue(result.userFriendlyMessage!!.contains("requires permission"))
    }

    @Test
    fun `SMS action passes when SEND_SMS permission is granted`() {
        val ctx = testContext(Manifest.permission.SEND_SMS)
        val result = ActionPermissionGate.checkPermissions(ctx, "SEND_SMS")
        assertTrue(result.isGranted)
        assertTrue(result.missingPermissions.isEmpty())
    }

    @Test
    fun `Location action passes if either fine or coarse location is granted`() {
        val deniedCtx = testContext()
        val deniedResult = ActionPermissionGate.checkPermissions(deniedCtx, "GET_LOCATION")
        assertFalse(deniedResult.isGranted)

        // Grant coarse location
        val coarseCtx = testContext(Manifest.permission.ACCESS_COARSE_LOCATION)
        val grantedResult = ActionPermissionGate.checkPermissions(coarseCtx, "GET_LOCATION")
        assertTrue(grantedResult.isGranted)

        // Grant fine location
        val fineCtx = testContext(Manifest.permission.ACCESS_FINE_LOCATION)
        val fineResult = ActionPermissionGate.checkPermissions(fineCtx, "GET_LOCATION")
        assertTrue(fineResult.isGranted)
    }

    @Test
    fun `Camera action fails when CAMERA permission is missing`() {
        val ctx = testContext()
        val result = ActionPermissionGate.checkPermissions(ctx, "TAKE_PHOTO")
        assertFalse(result.isGranted)
        assertTrue(result.missingPermissions.contains(Manifest.permission.CAMERA))
    }
}
