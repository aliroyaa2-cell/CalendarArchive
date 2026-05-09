package com.alaimtiaz.calendararchive.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val arabicLocale = Locale("ar")

    /** Format full date with year (always shows year since archive spans 25+ years) */
    fun formatFull(millis: Long, allDay: Boolean = false): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val now = Calendar.getInstance()

        val isSameDay = cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

        val isTomorrow = run {
            val tomorrow = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)
        }

        val isYesterday = run {
            val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        }

        val datePart = when {
            isSameDay -> "اليوم"
            isTomorrow -> "غداً"
            isYesterday -> "أمس"
            else -> SimpleDateFormat("EEEE، d MMM yyyy", arabicLocale).format(Date(millis))
        }

        return if (allDay) {
            "$datePart · يوم كامل"
        } else {
            val timePart = SimpleDateFormat("h:mm a", arabicLocale).format(Date(millis))
            "$datePart · $timePart"
        }
    }

    fun formatSourceLabel(accountType: String?, accountName: String?, calendarName: String?): String {
        val typeLabel = when {
            accountType.isNullOrBlank() -> ""
            accountType.contains("google", ignoreCase = true) -> "Google"
            accountType.contains("samsung", ignoreCase = true) -> "Samsung"
            accountType.contains("outlook", ignoreCase = true) ||
                    accountType.contains("microsoft", ignoreCase = true) -> "Outlook"
            accountType == "LOCAL" -> "محلي"
            else -> accountType.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }

        val parts = mutableListOf<String>()
        if (typeLabel.isNotEmpty()) parts.add(typeLabel)
        if (!calendarName.isNullOrBlank()) parts.add(calendarName)
        if (!accountName.isNullOrBlank() && accountName != calendarName) parts.add(accountName)

        return parts.joinToString(" · ")
    }
}
