package ui.model

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

class AndroidContext(
    activity: AppCompatActivity,
    override val coroutineScope: CoroutineScope,
) : AppContext {
    private val activityRef = WeakReference(activity)

    fun getAndroidNativeContext(): android.content.Context? = activityRef.get()

    private val _safeAreaInsetsFlow = MutableStateFlow(SafeAreaInsets(0f, 0f, 0f, 0f))
    val safeAreaInsetsFlow: StateFlow<SafeAreaInsets> = _safeAreaInsetsFlow

    fun setSafeAreaInsets(safeAreaInsets: SafeAreaInsets) {
        _safeAreaInsetsFlow.value = safeAreaInsets
    }
}

val AppContext.androidContext: AndroidContext
    get() = this as AndroidContext

val AppContext.androidNativeContext: android.content.Context
    get() = requireNotNull(androidContext.getAndroidNativeContext())
