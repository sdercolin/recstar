package com.sdercolin.recstar

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.Paths

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainView()
        }
        initializeApp()
    }


    private fun initializeApp() {
        Paths.initializeContext(this)
    }
}
