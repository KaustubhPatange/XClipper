package com.kpstv.xclipper.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
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
import com.kpstv.xclipper.extensions.collpase
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.ui.adapters.TagAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.dialog_create_tag.*
import kotlinx.android.synthetic.main.dialog_create_tag.view.*
import kotlinx.coroutines.delay
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.concurrent.schedule


class TagDialog : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val viewModelFactory: MainViewModelFactory by instance()

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private lateinit var adapter: TagAdapter
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_create_tag)

        setToolbar()

        setRecyclerView()

        btn_send.setOnClickListener {
            if (dct_editText.text.isNotBlank()) {
                mainViewModel.postToTagRepository(Tag.from(dct_editText.text.toString()))
                dct_editText.text.clear()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /** A Timeout creates a cool recyclerView loading effect */

        Coroutines.main {
            delay(DELAY_SPAN)
            bindUI()
        }
    }


    private fun bindUI() {
        mainViewModel.tagLiveData.observe(this, Observer {
            adapter.submitList(it)
        })

        mainViewModel.stateManager.dialogState.observe(this, Observer { state ->
            when (state) {
                DialogState.Normal -> {
                    dct_editLayout.dct_editText.text.clear()
                    dct_editLayout.collpase()
                }
                DialogState.Edit -> {
                    dct_editLayout.isEnabled = true
                    dct_editLayout.show()
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

        val switchCompat =
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
                mainViewModel.deleteFromTagRepository(tag)
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