package per.goweii.codex.android

import android.content.Context
import androidx.core.content.edit

class AppConfig(context: Context) {
    private val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    var scanFinder: String?
        get() = prefs.getString("scanFinder", null)
        set(value) = prefs.edit { putString("scanFinder", value) }

    var scanProcessor: String?
        get() = prefs.getString("scanProcessor", null)
        set(value) = prefs.edit { putString("scanProcessor", value) }

    var decodeProcessor: String?
        get() = prefs.getString("decodeProcessor", null)
        set(value) = prefs.edit { putString("decodeProcessor", value) }

    var encodeProcessor: String?
        get() = prefs.getString("encodeProcessor", null)
        set(value) = prefs.edit { putString("encodeProcessor", value) }

    companion object {
        @Volatile
        private lateinit var sInstance: AppConfig

        fun getInstance(context: Context): AppConfig {
            if (!::sInstance.isInitialized) {
                synchronized(AppConfig::class) {
                    if (!::sInstance.isInitialized) {
                        sInstance = AppConfig(context.applicationContext)
                    }
                }
            }
            return sInstance
        }
    }
}