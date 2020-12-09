package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.STAGGERED_SPAN_COUNT
import com.kpstv.xclipper.App.STAGGERED_SPAN_COUNT_MIN
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.extensions.drawableFrom
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.ui.adapters.EditAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_edit_layout.*
import kotlinx.coroutines.delay

@AndroidEntryPoint
class EditDialog : AppCompatActivity() {

    companion object {
        const val STATE_DIALOG_TEXT_FIELD = "state_dialog_text_field"
        const val STATE_TAG_RECYCLERVIEW = "state_tag_recyclerview"
    }

    private val mainViewModel: MainViewModel by viewModels()

    private var spanCount = 2

    private lateinit var clip: Clip
    private lateinit var adapter: EditAdapter
    private lateinit var edType: EDType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeUtils.setDialogTheme(this)

        setContentView(R.layout.dialog_edit_layout)

        toolbar.navigationIcon = drawableFrom(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        setRecyclerView()

        edType = if (mainViewModel.editManager.getClip() == null) EDType.Create
        else {

            /** Set the current clip for managing */
            clip = mainViewModel.editManager.getClip()!!

            de_editText.setText(clip.data)
            EDType.Edit
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        restoreData(savedInstanceState)

        /** A Timeout on binding creates a cool effect */

        Coroutines.main {
            delay(App.DELAY_SPAN)
            bindUI()
        }
    }

    fun saveClick(view: View) {
        val text = de_editText.text.toString()

        if (text.isNotBlank()) {
            if (edType == EDType.Edit) {
                performEditTask(text)
            } else {
                performCreateTask(text)
            }
        } else
            Toasty.error(this, getString(R.string.error_empty_text)).show()
    }

    private fun performEditTask(text: String) {
        mainViewModel.checkForDuplicateClip(text, clip.id!!, RepositoryListener(
            dataExist = Toasty.error(
                this,
                getString(R.string.error_duplicate_data)
            )::show,
            notFound = {
                mainViewModel.postUpdateToRepository(
                    clip,
                    /** In the second parameter we are also supplying the tags as well. */
                    clip.clone(text, mainViewModel.editManager.getSelectedTags())
                )
                postSuccess()
            }
        ))
    }

    private fun performCreateTask(text: String) {
        mainViewModel.checkForDuplicateClip(text,
            RepositoryListener(
                dataExist = Toasty.error(
                    this,
                    getString(R.string.error_duplicate_data)
                )::show,
                notFound = {
                    mainViewModel.postToRepository(
                        Clip.from(text, mainViewModel.editManager.getSelectedTags())
                    )
                    postSuccess()
                }
            ))
    }

    private fun postSuccess() {
        Toasty.info(this, getString(R.string.edit_success)).show()
        finish()
    }

    private fun setRecyclerView() {
        adapter = EditAdapter(
            viewLifecycleOwner = this,
            selectedTags = mainViewModel.editManager.selectedTags,
            onClick = { tag, _ ->
                mainViewModel.editManager.addOrRemoveSelectedTag(tag)
            }
        )

        refreshRecyclerView(spanCount)

        del_recyclerView.adapter = adapter
    }

    private fun bindUI() {
        mainViewModel.editManager.spanCount.observe(this, {
            refreshRecyclerView(it)
        })

        mainViewModel.editManager.tagFixedLiveData.observe(this, {
            if (it.size > 3)
                mainViewModel.editManager.postSpanCount(STAGGERED_SPAN_COUNT)
            else
                mainViewModel.editManager.postSpanCount(STAGGERED_SPAN_COUNT_MIN)
            adapter.submitList(it)
        })
    }

    private fun refreshRecyclerView(span: Int) {
        del_recyclerView.layoutManager =
            StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.HORIZONTAL)
    }

    private fun restoreData(savedInstanceState: Bundle?) {
        val previousData = savedInstanceState?.getString(STATE_DIALOG_TEXT_FIELD)

        if (previousData?.isNotEmpty() == true)
            de_editText.setText(previousData)
    }

    override fun onStop() {
        val bundle = Bundle().apply {
            if (de_editText.text.length < 1000)
                putString(STATE_DIALOG_TEXT_FIELD, de_editText.text.toString())
            putParcelable(STATE_TAG_RECYCLERVIEW, del_recyclerView.layoutManager?.onSaveInstanceState())
        }
        onSaveInstanceState(bundle)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.editManager.clearClip()
    }


    enum class EDType {
        Create,
        Edit
    }
}