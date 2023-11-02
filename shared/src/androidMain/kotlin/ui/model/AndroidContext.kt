package ui.model

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference

class AndroidContext(
    activity: AppCompatActivity,
    override val coroutineScope: CoroutineScope,
) : AppContext {
    private val activityRef = WeakReference(activity)

    fun getAndroidNativeContext(): android.content.Context? = activityRef.get()
}

val AppContext.androidContext: AndroidContext
    get() = this as AndroidContext

val AppContext.androidNativeContext: android.content.Context
    get() = requireNotNull(androidContext.getAndroidNativeContext())
