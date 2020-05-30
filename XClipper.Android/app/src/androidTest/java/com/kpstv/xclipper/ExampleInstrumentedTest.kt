package com.kpstv.xclipper

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kpstv.xclipper.data.model.AppPkg
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private val TAG = javaClass.simpleName
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val d = Build.MODEL
        val e = Build.VERSION.SDK_INT
       /* val also = appContext.packageManager.getInstalledApplications(0)
            .mapNotNull {
                AppPkg(
                    it.className,
                    it.packageName
                )
            }

        Log.e(TAG, "Data "+ also)*/

      //  assertEquals(pkgAppsList.size, 10)
    }
}
