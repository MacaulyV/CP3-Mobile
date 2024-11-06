package com.daniel.cineverse

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class DisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)


        val intent = Intent(this, DetalheSalvos::class.java)
        startActivity(intent)
    }
}
