package com.opendroid.ai.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

object GenericAppAutomator {

    private const val RETRY_TIMEOUT_MS = 5000L
    private const val INITIAL_INTERVAL_MS = 100L
    private const val MAX_INTERVAL_MS = 400L

    /**
     * Freshly launched apps (e.g. right after OPEN_APP or a self-contained action
     * like PLAY_YOUTUBE) are often cold-starting when the next automation step
     * runs. Polls [attempt] with an adaptive backoff starting at [INITIAL_INTERVAL_MS]
     * up to [MAX_INTERVAL_MS] until it succeeds or [RETRY_TIMEOUT_MS] elapses.
     * Cooperatively checks coroutine cancellation at every iteration.
     */
    private suspend fun retryUntilTimeout(attempt: () -> Boolean): Boolean {
        val deadline = SystemClock.elapsedRealtime() + RETRY_TIMEOUT_MS
        var currentInterval = INITIAL_INTERVAL_MS
        while (true) {
            kotlinx.coroutines.currentCoroutineContext().ensureActive()
            if (attempt()) return true
            val now = SystemClock.elapsedRealtime()
            if (now >= deadline) return false

            val remaining = deadline - now
            val waitTime = minOf(currentInterval, remaining)
            if (waitTime <= 0L) return false

            delay(waitTime)
            currentInterval = minOf(MAX_INTERVAL_MS, (currentInterval * 1.5).toLong())
        }
    }

    suspend fun clickText(text: String): Boolean = retryUntilTimeout {
        OpenDroidAccessibilityService.getInstance()?.findAndClick(text) == true
    }

    suspend fun clickId(viewId: String): Boolean = retryUntilTimeout {
        OpenDroidAccessibilityService.getInstance()?.findAndClickById(viewId) == true
    }

    suspend fun typeText(searchText: String, content: String): Boolean = retryUntilTimeout {
        OpenDroidAccessibilityService.getInstance()?.findAndType(searchText, content) == true
    }

    suspend fun typeId(viewId: String, content: String): Boolean = retryUntilTimeout {
        OpenDroidAccessibilityService.getInstance()?.findAndTypeById(viewId, content) == true
    }

    /**
     * Performs the IME 'enter/search/go' action on the currently focused editable
     * field — submits a search box after [typeText]/[typeId] fills it in. No polling
     * here: it's meant to run immediately after a successful type, once the field is
     * already known to exist.
     */
    fun pressEnter(): Boolean {
        val service = OpenDroidAccessibilityService.getInstance() ?: return false
        return service.performImeEnter()
    }

    fun scrapeScreen(): String {
        val service = OpenDroidAccessibilityService.getInstance() ?: return ""
        return service.getScreenText()
    }

    fun pressBack(): Boolean {
        val service = OpenDroidAccessibilityService.getInstance() ?: return false
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    fun pressHome(): Boolean {
        val service = OpenDroidAccessibilityService.getInstance() ?: return false
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    fun scroll(forward: Boolean): Boolean {
        val service = OpenDroidAccessibilityService.getInstance() ?: return false
        return service.performScroll(forward)
    }

    fun clickCoordinates(x: Float, y: Float): Boolean {
        val service = OpenDroidAccessibilityService.getInstance() ?: return false
        return service.clickCoordinates(x, y)
    }
}
