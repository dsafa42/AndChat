package com.chat.aoita

import android.app.Application
import android.content.Context
import android.os.Build
import java.io.File

/** 崩溃时把错误栈写入文件，下次启动由 [com.chat.aoita.ui.CrashScreen] 显示，方便没有电脑时排查。 */
class AoitaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            runCatching {
                crashFile(this).writeText(
                    "thread: ${thread.name}\n" +
                        "android: ${Build.VERSION.SDK_INT}\n" +
                        "device: ${Build.MANUFACTURER} ${Build.MODEL}\n\n" +
                        e.stackTraceToString()
                )
            }
            previous?.uncaughtException(thread, e)
        }
    }

    companion object {
        fun crashFile(context: Context) = File(context.filesDir, "last_crash.txt")
    }
}
