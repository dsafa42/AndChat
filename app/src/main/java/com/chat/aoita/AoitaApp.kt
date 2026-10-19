package com.chat.aoita

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/** 崩溃时把错误栈写入文件，下次启动由 MainActivity 显示，方便没有电脑时排查。 */
class AoitaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            try {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                File(filesDir, CRASH_FILE).writeText(
                    "thread: ${thread.name}\nandroid: ${android.os.Build.VERSION.SDK_INT}\n" +
                        "device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n\n$sw"
                )
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, e)
        }
    }

    companion object {
        const val CRASH_FILE = "last_crash.txt"
    }
}
