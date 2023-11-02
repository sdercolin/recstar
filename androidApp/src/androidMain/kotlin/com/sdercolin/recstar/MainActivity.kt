package com.sdercolin.recstar

import AppDependencies
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
        val dependencies = AppDependencies(context)
        setContent {
            MainView(dependencies)
        }
    }

    private fun initializeApp() {
        Paths.initializeContext(this)
        ensurePaths()
        Log.initialize()
    }
}
