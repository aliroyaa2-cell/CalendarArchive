package com.alaimtiaz.calendararchive

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Settings Screen
 *
 * Displays toggles and actions for app configuration.
 * All flags default to OFF — app behaves like stable v1.0.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var displayContainer: LinearLayout
    private lateinit var dataContainer: LinearLayout

    /**
     * SAF picker for choosing where to save the JSON export.
     * User picks file location + name; we receive the URI to write to.
     */
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            performExport(uri)
        } else {
            Toast.makeText(this, "تم إلغاء التصدير", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        displayContainer = findViewById(R.id.displaySettingsContainer)
        dataContainer = findViewById(R.id.dataSettingsContainer)

        renderDisplaySettings()
        renderDataSettings()
    }

    private fun renderDisplaySettings() {
        // Toggle 1: Hide source line
        addToggle(
            container = displayContainer,
            title = "إخفاء المصدر تحت الحدث",
            description = "يخفي السطر اللي فيه Google · الحساب · البريد تحت كل حدث.\n" +
                    "العنوان والوقت يبقون ظاهرين.",
            isChecked = FeatureFlags.isHideSourceEnabled(this),
            onChange = { enabled ->
                FeatureFlags.setHideSourceEnabled(this, enabled)
            }
        )

        // Toggle 2: Unified list (single scrollable list with two sections)
        addToggle(
            container = displayContainer,
            title = "قائمة موحدة (بدل التبويبات)",
            description = "تشيل التبويبات وتعرض كل الأحداث في قائمة واحدة بقسمين:\n" +
                    "📅 القادمة (أسود) + 🕐 السابقة (رمادي).\n" +
                    "البحث يشتغل على القسمين دفعة واحدة.",
            isChecked = FeatureFlags.isUnifiedListEnabled(this),
            onChange = { enabled ->
                FeatureFlags.setUnifiedListEnabled(this, enabled)
            }
        )
    }

    private fun renderDataSettings() {
        addExportButton(dataContainer)
    }

    private fun addExportButton(container: LinearLayout) {
        // Row container
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setBackgroundColor(0xFF1F1F1F.toInt())
        }

        // Title
        val titleView = TextView(this).apply {
            text = "تصدير الأرشيف"
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
        }

        // Description
        val descView = TextView(this).apply {
            text = "احفظ نسخة من كل أحداث الأرشيف (قادمة + سابقة) كملف JSON.\n" +
                    "تختار اسم الملف ومكان الحفظ في الخطوة التالية."
            textSize = 12f
            setTextColor(0xFFBDBDBD.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(10)
            }
        }

        // Export button
        val exportBtn = Button(this).apply {
            text = "اختر مكان الحفظ"
            textSize = 14f
            setOnClickListener {
                launchExportPicker()
            }
        }

        row.addView(titleView)
        row.addView(descView)
        row.addView(exportBtn)
        container.addView(row)
    }

    private fun launchExportPicker() {
        try {
            createDocumentLauncher.launch(ExportManager.suggestedFilename())
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر فتح متصفح الملفات: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun performExport(uri: Uri) {
        Toast.makeText(this, "جارٍ التصدير…", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                ExportManager.exportToUri(this@SettingsActivity, uri)
            }

            if (count >= 0) {
                Toast.makeText(
                    this@SettingsActivity,
                    "تم تصدير $count حدث بنجاح ✓",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@SettingsActivity,
                    "فشل التصدير — حاول مرة ثانية",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Add a single toggle row to the given container.
     * Uses plain android.widget.Switch for theme stability.
     */
    private fun addToggle(
        container: LinearLayout,
        title: String,
        description: String,
        isChecked: Boolean,
        onChange: (Boolean) -> Unit
    ) {
        // Container row
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setBackgroundColor(0xFF1F1F1F.toInt())
        }

        // Texts container (left side, takes remaining space)
        val textsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
        }

        val descView = TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(0xFFBDBDBD.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        textsContainer.addView(titleView)
        textsContainer.addView(descView)

        // Plain Switch (right side)
        val switchView = Switch(this).apply {
            this.isChecked = isChecked
            setOnCheckedChangeListener { _, checked ->
                onChange(checked)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(12)
            }
        }

        row.addView(textsContainer)
        row.addView(switchView)

        container.addView(row)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
