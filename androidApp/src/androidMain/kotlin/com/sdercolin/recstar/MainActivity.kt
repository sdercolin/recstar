package com.sdercolin.recstar

import AppDependencies
import MainView
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.Paths
import io.ensurePaths
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import model.AppPreference
import model.licenseReportResId
import ui.model.AndroidContext
import util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeApp()
        val context = AndroidContext(this, lifecycleScope)
        val dependencies = AppDependencies(context)
        observeOrientation(dependencies)
        setContent {
            MainView(dependencies)
        }
    }

    private fun observeOrientation(dependencies: AppDependencies) {
        val appPreferenceRepository = dependencies.appPreferenceRepository
        lifecycleScope.launch {
            appPreferenceRepository.flow.map { it.orientation }.distinctUntilChanged().collectLatest { orientation ->
                requestedOrientation = when (orientation) {
                    AppPreference.ScreenOrientation.Auto -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    AppPreference.ScreenOrientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    AppPreference.ScreenOrientation.Landscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        }
    }

    private fun initializeApp() {
        Paths.initializeContext(this)
        ensurePaths()
        Log.initialize()
        licenseReportResId = R.raw.license_report
    }
}
