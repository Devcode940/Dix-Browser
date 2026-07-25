package com.devcode940.web

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.devcode940.web.page.browser.BrowserActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tabButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabButton = findViewById(R.id.new_tab_button)
        tabButton.setOnClickListener {
            val intent = Intent(this, BrowserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
