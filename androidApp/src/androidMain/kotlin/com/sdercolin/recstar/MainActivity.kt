package com.sdercolin.recstar

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.Paths
import io.ensurePaths
import ui.model.AndroidContext
import util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeApp()
        val context = AndroidContext(this, lifecycleScope)
        setContent {
            MainView(context)
        }
    }

    private fun initializeApp() {
        Paths.initializeContext(this)
        ensurePaths()
        Log.initialize()
    }
}
