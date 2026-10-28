package com.ahnaf.floatingavatar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }
    private lateinit var targetInput: EditText
    private lateinit var avatarInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
        }
        root.addView(TextView(this).apply {
            text = "Floating Avatar Launcher"
            textSize = 24f
        })
        root.addView(TextView(this).apply {
            text = "Clicking the bubble opens the app package name or website below."
            textSize = 14f
            setPadding(0, 12, 0, 18)
        })
        root.addView(TextView(this).apply { text = "Website URL or Android app package name" })
        targetInput = EditText(this).apply {
            hint = "https://chatgpt.com"
            setText(prefs.getString("target", "https://chatgpt.com"))
            singleLine = true
        }
        root.addView(targetInput, ViewGroup.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "Bubble label (1–2 characters)"
            setPadding(0, 12, 0, 0)
        })
        avatarInput = EditText(this).apply {
            hint = "A"
            setText(prefs.getString("label", "A"))
            singleLine = true
        }
        root.addView(avatarInput, ViewGroup.LayoutParams(-1, -2))

        val permissionButton = Button(this).apply {
            text = "Grant display-over-apps permission"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                } else Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(permissionButton)
        root.addView(Button(this).apply {
            text = "Save & Show Floating Avatar"
            setOnClickListener {
                val target = targetInput.text.toString().trim()
                if (target.isBlank()) {
                    Toast.makeText(this@MainActivity, "Enter a website URL or app package name", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                prefs.edit().putString("target", target)
                    .putString("label", avatarInput.text.toString().trim().ifBlank { "A" }.take(2)).apply()
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                    Toast.makeText(this@MainActivity, "Enable permission, then tap this button again.", Toast.LENGTH_LONG).show()
                } else {
                    startService(Intent(this@MainActivity, BubbleService::class.java))
                    Toast.makeText(this@MainActivity, "Floating avatar started", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        })
        root.addView(Button(this).apply {
            text = "Stop Floating Avatar"
            setOnClickListener { stopService(Intent(this@MainActivity, BubbleService::class.java)) }
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
