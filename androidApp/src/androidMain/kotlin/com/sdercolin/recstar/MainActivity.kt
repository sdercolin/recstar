package com.sdercolin.recstar

import AppDependencies
import MainView
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import ui.model.SafeAreaInsets
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
        setImmersive(context)
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

    private fun setImmersive(androidContext: AndroidContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setImmersiveR(androidContext)
        } else {
            setImmersiveLegacy(androidContext)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setImmersiveR(androidContext: AndroidContext) {
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            val safeAreaInsets = SafeAreaInsets(
                top = insets.top.toFloat(),
                left = insets.left.toFloat(),
                bottom = insets.bottom.toFloat(),
                right = insets.right.toFloat(),
            )
            androidContext.setSafeAreaInsets(safeAreaInsets)
            windowInsets
        }
    }

    @Suppress("DEPRECATION")
    private fun setImmersiveLegacy(androidContext: AndroidContext) {
        window.decorView.systemUiVisibility = (
            // Layouts will be drawn under the status bar
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                // Layouts will be drawn under the navigation bar
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )

        window.decorView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val safeAreaInsets = SafeAreaInsets(
                top = windowInsets.systemWindowInsetTop.toFloat(),
                left = windowInsets.systemWindowInsetLeft.toFloat(),
                bottom = windowInsets.systemWindowInsetBottom.toFloat(),
                right = windowInsets.systemWindowInsetRight.toFloat(),
            )
            androidContext.setSafeAreaInsets(safeAreaInsets)
            windowInsets
        }
    }
}
