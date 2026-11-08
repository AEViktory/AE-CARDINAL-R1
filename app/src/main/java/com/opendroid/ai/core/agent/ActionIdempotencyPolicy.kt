package com.opendroid.ai.core.agent

/**
 * Idempotency classification for AE:CARDINAL autonomous actions.
 *
 * Rules:
 * - IDEMPOTENT: Safe to repeat on network retry or uncertainty without adverse side effects
 *   (e.g., read-only informational actions, idempotent setters like setting brightness/volume/wifi).
 * - NON_IDEMPOTENT: Has real-world side effects or financial/communication impact
 *   (e.g., sending SMS, making calls, sending emails, posting social media, booking transport, deleting files).
 * - UNKNOWN: Fallback classification. Must be treated conservatively as NON_IDEMPOTENT.
 */
enum class Idempotency {
    IDEMPOTENT,
    NON_IDEMPOTENT,
    UNKNOWN
}

object ActionIdempotencyPolicy {

    private val IDEMPOTENT_ACTIONS = setOf(
        // Informational & Read-Only
        "GET_WEATHER", "GET_NEWS", "CALCULATE", "CURRENCY_CONVERT",
        "TRANSLATE", "WEB_SEARCH", "SUMMARIZE_URL", "CHECK_STOCK",
        "DEFINE_WORD", "CONVERT_UNITS", "FACT_CHECK", "GET_SYSTEM_INFO",
        "CHECK_TRAFFIC", "CHECK_FLIGHT", "TRACK_DELIVERY", "CHECK_BALANCE",
        "LIST_CALENDAR_TODAY", "LIST_CALENDAR_WEEK", "READ_MESSAGES",
        "READ_EMAILS", "READ_NOTES", "READ_FILE", "LIST_FILES",
        "GET_SCREEN_TEXT", "LIST_INSTALLED_APPS", "ANALYZE_SCREENSHOT",
        "TAKE_SCREENSHOT", "TAKE_SCREEN_SHOT",
        "READ_AND_REMEMBER_SCREEN", "RECALL_MEMORY", "QUERY_KNOWLEDGE_GRAPH",
        // Idempotent System Controls & Navigation
        "OPEN_APP", "CLOSE_APP", "SET_VOLUME", "SET_BRIGHTNESS",
        "TOGGLE_WIFI", "TOGGLE_BLUETOOTH", "TOGGLE_FLASHLIGHT",
        "SET_RINGER_MODE", "TOGGLE_DND", "SCROLL", "WAIT"
    )

    private val NON_IDEMPOTENT_ACTIONS = setOf(
        // Communication
        "SEND_SMS", "SEND_EMAIL", "SEND_WHATSAPP", "SEND_TELEGRAM",
        "MAKE_CALL", "MAKE_VIDEO_CALL", "ANSWER_CALL", "DECLINE_CALL",
        // Transport & Booking
        "BOOK_UBER", "BOOK_OLA",
        // Financial
        "TRANSFER_MONEY", "PAY_BILL",
        // Social
        "CREATE_POST", "PUBLISH_POST", "SEND_MESSAGE",
        // Destructive / State Mutations
        "DELETE_FILE", "CREATE_FILE", "WRITE_FILE", "APPEND_FILE",
        "CREATE_CALENDAR_EVENT", "DELETE_CALENDAR_EVENT", "SET_ALARM"
    )

    /**
     * Determines idempotency of an action by its canonical name.
     */
    fun classify(actionName: String): Idempotency {
        val upper = actionName.uppercase().trim()
        return when {
            upper in IDEMPOTENT_ACTIONS -> Idempotency.IDEMPOTENT
            upper in NON_IDEMPOTENT_ACTIONS -> Idempotency.NON_IDEMPOTENT
            else -> Idempotency.UNKNOWN
        }
    }

    /**
     * Decides whether an action can be safely retried automatically when its prior result was failed or ambiguous.
     */
    fun canSafelyRetry(actionName: String): Boolean {
        return classify(actionName) == Idempotency.IDEMPOTENT
    }
}
