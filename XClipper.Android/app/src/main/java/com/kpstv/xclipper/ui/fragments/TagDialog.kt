package com.kpstv.xclipper.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.DialogState
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.collpase
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.ui.adapters.TagAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.dialog_create_tag.view.*


class TagDialog(
    private val viewModel: MainViewModel,
    private val onItemClick: (Tag) -> Unit
) : DialogFragment() {

    private lateinit var adapter: TagAdapter
    private val TAG = javaClass.simpleName

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_create_tag, null)

        with(view) {

            setToolbar(this)

            setRecyclerView(this)

            bindUI(this)

            btn_send.setOnClickListener {
                if (dct_editText.text.isNotBlank()) {
                    viewModel.postToTagRepository(Tag.from(dct_editText.text.toString()))
                    dct_editText.text.clear()
                }
            }

            return AlertDialog.Builder(context).apply {
                setView(view)
            }.create()
        }
    }

    private fun bindUI(view: View) = with(view) {
        viewModel.tagLiveData.observe(context as LifecycleOwner, Observer {
           // Log.e(TAG, "List Size: ${it.size}")
            adapter.submitList(it)
        })

        viewModel.stateManager.dialogState.observe(context as LifecycleOwner, Observer { state ->
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

    private fun setToolbar(view: View) = with(view) {
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.inflateMenu(R.menu.dct_menu)

        val switchCompat =
            LayoutInflater.from(context).inflate(R.layout.switch_layout, null) as SwitchCompat
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                viewModel.stateManager.setDialogState(DialogState.Edit)
            else
                viewModel.stateManager.setDialogState(DialogState.Normal)
        }

        toolbar.menu.findItem(R.id.action_edit).actionView = switchCompat
    }

    private fun setRecyclerView(view: View) = with(view) {
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        dct_recycler_view.layoutManager = layoutManager


        adapter = TagAdapter(
            dialogState = viewModel.stateManager.dialogState,
            tagFilter = viewModel.searchManager.tagFilters,
            onCloseClick = { tag, _ ->
                viewModel.deleteFromTagRepository(tag)
            },
            onClick = { tag, _ ->
                onItemClick.invoke(tag)
                viewModel.searchManager.addTagFilter(tag)
                dismiss()
            }
        )
        dct_recycler_view.adapter = adapter
    }

    override fun onDestroy() {
        viewModel.stateManager.setDialogState(DialogState.Normal)
        super.onDestroy()
    }

}