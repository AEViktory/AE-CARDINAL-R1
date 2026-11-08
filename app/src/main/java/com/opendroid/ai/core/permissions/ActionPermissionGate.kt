package com.opendroid.ai.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Pre-execution permission verification for AE:CARDINAL autonomous actions.
 *
 * Verifies that the required runtime permissions are actually GRANTED before
 * an action attempts to access hardware or sensitive Android providers.
 * Differentiates between declared permissions in AndroidManifest.xml and granted
 * permissions at runtime.
 */
object ActionPermissionGate {

    data class CheckResult(
        val isGranted: Boolean,
        val missingPermissions: List<String>,
        val userFriendlyMessage: String? = null
    )

    private val ACTION_REQUIRED_PERMISSIONS: Map<String, List<String>> = mapOf(
        // Telephony & SMS
        "SEND_SMS" to listOf(Manifest.permission.SEND_SMS),
        "READ_MESSAGES" to listOf(Manifest.permission.READ_SMS),
        "MAKE_CALL" to listOf(Manifest.permission.CALL_PHONE),
        "MAKE_PHONE_CALL" to listOf(Manifest.permission.CALL_PHONE),
        "READ_PHONE_STATE" to listOf(Manifest.permission.READ_PHONE_STATE),

        // Contacts & Calendar
        "READ_CONTACTS" to listOf(Manifest.permission.READ_CONTACTS),
        "WRITE_CONTACTS" to listOf(Manifest.permission.WRITE_CONTACTS),
        "READ_CALENDAR" to listOf(Manifest.permission.READ_CALENDAR),
        "LIST_CALENDAR_TODAY" to listOf(Manifest.permission.READ_CALENDAR),
        "LIST_CALENDAR_WEEK" to listOf(Manifest.permission.READ_CALENDAR),

        // Hardware (Camera & Mic)
        "TAKE_PHOTO" to listOf(Manifest.permission.CAMERA),
        "RECORD_AUDIO" to listOf(Manifest.permission.RECORD_AUDIO),

        // Location
        "GET_LOCATION" to listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    /**
     * Checks whether all required runtime permissions for [actionName] are granted in [context].
     * For location, either FINE or COARSE is considered sufficient.
     */
    fun checkPermissions(context: Context, actionName: String): CheckResult {
        val required = ACTION_REQUIRED_PERMISSIONS[actionName.uppercase().trim()]
            ?: return CheckResult(isGranted = true, missingPermissions = emptyList())

        // Special case for location: either coarse or fine is acceptable
        if (actionName.equals("GET_LOCATION", ignoreCase = true)) {
            val hasFine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            return if (hasFine || hasCoarse) {
                CheckResult(isGranted = true, missingPermissions = emptyList())
            } else {
                CheckResult(
                    isGranted = false,
                    missingPermissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    userFriendlyMessage = "Action '$actionName' requires location permission which has not been granted."
                )
            }
        }

        val missing = required.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        return if (missing.isEmpty()) {
            CheckResult(isGranted = true, missingPermissions = emptyList())
        } else {
            val missingNames = missing.joinToString { it.substringAfterLast('.') }
            CheckResult(
                isGranted = false,
                missingPermissions = missing,
                userFriendlyMessage = "Action '$actionName' requires permission(s) [$missingNames] which have not been granted."
            )
        }
    }
}
