package com.example.miniarcade

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HowToPlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_howtoplay)

        val btnBack: Button = findViewById(R.id.btnBackFromHowToPlay)

        btnBack.setOnClickListener {
            Intent(this, MainActivity::class.java).also{
                startActivity(it)
            }
        }

    }


}