package com.sdercolin.recstar

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.Paths
import io.ensurePaths
import util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeApp()
        setContent {
            MainView()
        }
    }

    private fun initializeApp() {
        Paths.initializeContext(this)
        ensurePaths()
        Log.initialize()
    }
}
