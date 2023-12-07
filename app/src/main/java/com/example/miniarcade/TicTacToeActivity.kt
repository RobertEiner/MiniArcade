package com.example.miniarcade

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class TicTacToeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayMetrics = resources.displayMetrics
        val myTicTacToe = TicTacToe(this, displayMetrics.widthPixels, displayMetrics.heightPixels)

        setContentView(myTicTacToe)





    }


}