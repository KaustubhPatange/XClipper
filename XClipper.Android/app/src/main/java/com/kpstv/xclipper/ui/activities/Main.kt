package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kpstv.xclipper.R
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance


class Main : AppCompatActivity(), KodeinAware {

    override val kodein by closestKodein()
    private val viewModelFactory: MainViewModelFactory by instance()

    private val TAG = javaClass.name

    private lateinit var mainViewModel: MainViewModel/* by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }*/

    private lateinit var mTestView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        mainViewModel.clipLiveData.observeForever {

        }
        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
              val intent = Intent(
                  Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:$packageName")
              )
              startActivityForResult(intent, 0)
          }


          val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

          val layoutParams =
              WindowManager.LayoutParams(WindowManager.LayoutParams.FIRST_SUB_WINDOW)
          layoutParams.width = 300
          layoutParams.height = 300

          layoutParams.format = PixelFormat.RGBA_8888
          layoutParams.flags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                  or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
          layoutParams.token = window.decorView.windowToken
          layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY ;
          //Feel free to inflate here

          //Feel free to inflate here
          mTestView = View(this)
          mTestView.setBackgroundColor(Color.RED)

          //Must wire up back button, otherwise it's not sent to our activity

          //Must wire up back button, otherwise it's not sent to our activity
          mTestView.setOnClickListener {
              onBackPressed()
          }
          mTestView.onFocusChangeListener = View.OnFocusChangeListener { v, b ->
              Log.e(TAG, "Primary Clip: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
          }
          windowManager.addView(mTestView, layoutParams)*/
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mTestView != null) {
            val windowManager =
                baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (mTestView.isShown) {
                windowManager.removeViewImmediate(mTestView)
            }
        }
    }
}
