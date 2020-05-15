package com.kpstv.xclipper.ui.activities

import android.annotation.TargetApi
import android.content.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.cloneForAdapter
import com.kpstv.xclipper.ui.adapters.CIAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class Main : AppCompatActivity(), KodeinAware {

    private val TAG = javaClass.simpleName

    override val kodein by kodein()
    private val viewModelFactory: MainViewModelFactory by instance()

    private val clipboardManager: ClipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private lateinit var adapter: CIAdapter

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setRecyclerView()

        bindUI()

        checkClipboardData()

        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
              val intent = Intent(
                  Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:$packageName")
              )
              startActivityForResult(intent, 0)
          }

        */
    }


    private fun bindUI() = Coroutines.main {
        mainViewModel.clipLiveData.await().observeForever {
            adapter.submitList(it.cloneForAdapter().reversed())
            Log.e(TAG, "LiveData changed()")
        }
    }

    private fun setRecyclerView() {
        adapter = CIAdapter(context = this, onClick = { model, pos ->
            for ((i, e) in adapter.list.withIndex()) {
                if (i != pos && e.toDisplay) {
                    e.toDisplay = false
                    adapter.notifyItemChanged(i)
                }
            }
            model.toDisplay = !model.toDisplay
            adapter.notifyItemChanged(pos)
        })

        adapter.setCopyClick { clip, _ ->
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, clip.data?.Decrypt()))
        }

        adapter.setMenuItemClick { clip, i, menuType ->
           /* when (menuType) {
                CIAdapter.MENU_TYPE.Edit -> TODO()
                CIAdapter.MENU_TYPE.Delete -> TODO()
                CIAdapter.MENU_TYPE.Share -> TODO()
            }*/
        }

        ci_recyclerView.layoutManager = LinearLayoutManager(this)
        ci_recyclerView.adapter = adapter
        ci_recyclerView.setHasFixedSize(true)


    }


    /**
     * So I found out that sometimes in Android 10, clipboard still not get captured using my
     * accessibility service hack. To fix this whenever app is launched or come back from
     * background it will check & update the database with the clipboard.
     */
    private fun checkClipboardData() {
        val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        if (data!= null && CLIP_DATA != data) {
            CLIP_DATA = data

            mainViewModel.postToRepository(data)
            
            Log.e(TAG, "Pushed: $data")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkClipboardData()
    }
}
