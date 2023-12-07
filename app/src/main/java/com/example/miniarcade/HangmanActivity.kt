package com.example.miniarcade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity



class HangmanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = resources.displayMetrics
        val myHangman = Hangman(this, displayMetrics.widthPixels, displayMetrics.heightPixels)

        setContentView(myHangman)



    }
}