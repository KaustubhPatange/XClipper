package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.kpstv.xclipper.App.DELAY_SPAN
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.DialogState
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.ui.adapters.TagAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_create_tag.*
import kotlinx.android.synthetic.main.dialog_create_tag.view.*
import kotlinx.coroutines.delay

@AndroidEntryPoint
class TagDialog : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var adapter: TagAdapter
    private lateinit var switchCompat: SwitchCompat

    companion object {
        const val RESULT_CODE = 1
        const val TAG_DATA = "tag_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeUtils.setDialogTheme(this)

        setContentView(R.layout.dialog_create_tag)

        setToolbar()

        setRecyclerView()

        dct_editText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendButton()
                    return true
                }
                return false
            }

        })
        btn_send.setOnClickListener {
            sendButton()
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /** A Timeout on binding creates a cool effect */

        Coroutines.main {
            delay(DELAY_SPAN)
            bindUI()
            linearLayout1.minimumHeight = resources.getDimension(R.dimen.dimen170).toInt()
        }
    }

    private fun sendButton() {
        if (dct_editText.text.isNotBlank()) {
            mainViewModel.postToTagRepository(Tag.from(dct_editText.text.trim().toString()))
            dct_editText.text.clear()
        }
    }

    private fun bindUI() {
        mainViewModel.tagLiveData.observe(this, {
            if (it.isEmpty()) mainViewModel.stateManager.setDialogState(DialogState.Edit)
            adapter.submitList(it)
        })

        mainViewModel.stateManager.dialogState.observe(this, { state ->
            when (state) {
                DialogState.Normal -> {
                    dct_editLayout.dct_editText.text.clear()
                    dct_editLayout.collapse()
                    switchCompat.isChecked = false
                }
                DialogState.Edit -> {
                    dct_editLayout.isEnabled = true
                    dct_editLayout.show()
                    switchCompat.isChecked = true
                    dct_editText.requestFocus()
                }
                else -> {
                    // When exhaustive
                }
            }
        })
    }

    private fun setToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        toolbar.inflateMenu(R.menu.dct_menu)

        switchCompat =
            LayoutInflater.from(this).inflate(R.layout.switch_layout, null) as SwitchCompat
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                mainViewModel.stateManager.setDialogState(DialogState.Edit)
            else
                mainViewModel.stateManager.setDialogState(DialogState.Normal)
        }

        toolbar.menu.findItem(R.id.action_edit).actionView = switchCompat
    }

    private fun setRecyclerView() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        dct_recycler_view.layoutManager = layoutManager

        adapter = TagAdapter(
            lifecycleOwner = this,
            dialogState = mainViewModel.stateManager.dialogState,
            tagFilter = mainViewModel.searchManager.tagFilters,
            tagMapData = mainViewModel.tagCountData,
            onCloseClick = { tag, _ ->
                mainViewModel.deleteFromTagRepository(
                    tag, StatusListener(
                        onComplete = { },
                        onError = Toasty.error(this, getString(R.string.error_tag_dependent))::show
                    )
                )
            },
            onClick = { tag, _ ->
                if (mainViewModel.stateManager.isEditDialogStateActive()) return@TagAdapter

                if (mainViewModel.searchManager.existTagFilter(tag)) {
                    mainViewModel.searchManager.removeTagFilter(tag)
                }else {
                    mainViewModel.searchManager.addTagFilter(tag)
                }

                finish()
            }
        )
        dct_recycler_view.adapter = adapter
    }

    override fun onDestroy() {
        mainViewModel.stateManager.setDialogState(DialogState.Normal)
        super.onDestroy()
    }
}