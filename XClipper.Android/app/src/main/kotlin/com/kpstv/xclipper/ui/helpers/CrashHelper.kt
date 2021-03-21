package com.kpstv.xclipper.ui.helpers

import android.app.Application
import android.content.Context
import com.kpstv.xclipper.ui.activities.Crash

/**
 * A class created to manage app crashes and report to our internal database.
 */
class CrashHelper {
   companion object {
       private var application: Application? = null

       fun inject(context: Context) {
           this.application = context.applicationContext as Application

           val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
           Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
               Crash.launch(application, throwable)
               oldHandler?.uncaughtException(thread, throwable)
           }
       }
   }
}