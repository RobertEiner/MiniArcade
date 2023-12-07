package com.example.miniarcade

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val imgViewTicTacToe: ImageView = findViewById(R.id.imgViewTicTacToe)
        imgViewTicTacToe.setOnClickListener {
            Intent(this, TicTacToeActivity::class.java).also {
                startActivity(it)
            }
            Log.d("image view clicked", "")
        }

        val imgViewHangman: ImageView = findViewById(R.id.imgViewHangman)
        imgViewHangman.setOnClickListener {
            Intent(this, HangmanActivity::class.java). also {
                startActivity(it)
            }
        }

        val imgViewMemory: ImageView = findViewById(R.id.imgViewMemory)
        imgViewMemory.setOnClickListener {
            Intent(this, MemoryActivity::class.java).also {
                startActivity(it)
            }
        }




    }
}