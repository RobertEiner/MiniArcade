package com.example.miniarcade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MemoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayMetrics = resources.displayMetrics
        val myMemory = Memory(this, displayMetrics.widthPixels, displayMetrics.heightPixels)

        setContentView(myMemory)



    }


}