package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.kpstv.xclipper.App.DELAY_SPAN
import com.kpstv.xclipper.App.TAG_DIALOG_RESULT_CODE
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.DialogState
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.ui.adapters.TagAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_create_tag.*
import kotlinx.android.synthetic.main.dialog_create_tag.view.*
import kotlinx.coroutines.delay
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class TagDialog : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val viewModelFactory by instance<MainViewModelFactory>()

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private lateinit var adapter: TagAdapter
    private val TAG = javaClass.simpleName
    private lateinit var switchCompat: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_create_tag)

        setToolbar()

        setRecyclerView()

        dct_editText.setOnEditorActionListener(object: TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendButton()
                    return true;
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
            linearLayout1.minimumHeight =  resources.getDimension(R.dimen.dimen170).toInt()
        }
    }

    private fun sendButton() {
        if (dct_editText.text.isNotBlank()) {
            mainViewModel.postToTagRepository(Tag.from(dct_editText.text.trim().toString()))
            dct_editText.text.clear()
        }
    }

    private fun bindUI() {
        mainViewModel.tagLiveData.observe(this, Observer {
            if (it.isEmpty()) mainViewModel.stateManager.setDialogState(DialogState.Edit)
            adapter.submitList(it)
        })

        mainViewModel.stateManager.dialogState.observe(this, Observer { state ->
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
                    // TODO: When exhaustive
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

    private fun setRecyclerView()  {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        dct_recycler_view.layoutManager = layoutManager

        adapter = TagAdapter(
            dialogState = mainViewModel.stateManager.dialogState,
            tagFilter = mainViewModel.searchManager.tagFilters,
            onCloseClick = { tag, _ ->
                mainViewModel.deleteFromTagRepository(tag, StatusListener(
                    onComplete = { },
                    onError = Toasty.error(this, getString(R.string.error_tag_dependent))::show
                ))
            },
            onClick = { tag, _ ->
                if (mainViewModel.stateManager.isEditDialogStateActive()) return@TagAdapter

                mainViewModel.setTag(tag)
                mainViewModel.searchManager.addTagFilter(tag)
                setResult(TAG_DIALOG_RESULT_CODE)
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