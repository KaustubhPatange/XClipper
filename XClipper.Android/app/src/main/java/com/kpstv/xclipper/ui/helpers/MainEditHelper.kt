package com.kpstv.xclipper.ui.helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.kpstv.license.Decrypt
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.ui.adapters.EditAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_edit_layout.view.*
import kotlinx.android.synthetic.main.tag_item.view.*

class MainEditHelper(
    private val context: Context,
    private val viewModel: MainViewModel
) {
    private lateinit var dialog: AlertDialog
    private lateinit var adapter: EditAdapter
    private lateinit var clip: Clip
    /**
     * Call this function to execute the edit event.
     */
    fun show(clip: Clip) =
        with(context) {

            this@MainEditHelper.clip = clip
            viewModel.editManager.postClip(clip)

            val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_layout, null)

            view.toolbar.navigationIcon = getDrawable(R.drawable.ic_close)
            view.toolbar.setNavigationOnClickListener {
                dialog.dismiss()
            }

            view.de_editText.setText(clip.data?.Decrypt())

            setRecyclerView(view)

            bindUI(view)

            dialog = AlertDialog.Builder(this).apply {
                setView(view)
                setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val text = view.de_editText.text.toString()

                    if (text.isNotBlank()) {
                        viewModel.postUpdateToRepository(clip, clip.clone(text.Encrypt(), viewModel.editManager.getSelectedTags()))
                        Toasty.info(this@MainEditHelper.context, getString(R.string.edit_success)).show()
                        dialog.dismiss()
                    }else
                        Toasty.error(this@MainEditHelper.context, getString(R.string.error_empty_text)).show()
                }
            }.create()

            dialog.show()
        }

    private fun bindUI(view: View) = with(view) {
        viewModel.editManager.tagFixedLiveData.observe(context as LifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun setRecyclerView(view: View) = with(view) {
        adapter = EditAdapter(
            viewLifecycleOwner = this@MainEditHelper.context as LifecycleOwner,
            selectedTags = viewModel.editManager.selectedTags,
            onClick = { tag, _ ->
                viewModel.editManager.addOrRemoveSelectedTag(tag)
            }
        )

        del_recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        del_recyclerView.adapter = adapter
    }
}