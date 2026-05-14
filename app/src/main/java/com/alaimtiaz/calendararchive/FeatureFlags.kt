package com.alaimtiaz.calendararchive

import android.content.Context
import android.content.SharedPreferences

/**
 * Feature Flags — runtime toggles for new/experimental features.
 *
 * Default: ALL FLAGS OFF — app behaves identically to stable v1.0.
 * Toggle ON to enable new behavior.
 * Toggle OFF to instantly revert to original behavior (no restart needed).
 */
object FeatureFlags {

    private const val PREFS_NAME = "feature_flags"

    // ━━━ Flag keys ━━━
    private const val KEY_HIDE_SOURCE = "flag_hide_source"
    private const val KEY_UNIFIED_LIST = "flag_unified_list"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ━━━ Public API ━━━

    /**
     * Hide the source line (Google · account · email) under each event.
     * Default: OFF (source visible — original behavior).
     */
    fun isHideSourceEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_HIDE_SOURCE, false)
    }

    fun setHideSourceEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HIDE_SOURCE, enabled).apply()
    }

    /**
     * Show all events in a unified list with two sections (upcoming + past)
     * instead of separate tabs. Search works across both at once.
     * Default: OFF (tabs — original behavior).
     */
    fun isUnifiedListEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_UNIFIED_LIST, false)
    }

    fun setUnifiedListEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_UNIFIED_LIST, enabled).apply()
    }
}
