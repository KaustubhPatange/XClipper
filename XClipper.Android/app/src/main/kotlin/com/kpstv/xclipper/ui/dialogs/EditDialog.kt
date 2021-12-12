package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.databinding.DialogEditLayoutBinding
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.extensions.drawableFrom
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.adapters.EditAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditDialog : AppCompatActivity() {

    companion object {
        const val STATE_DIALOG_TEXT_FIELD = "state_dialog_text_field"
        const val STATE_TAG_RECYCLERVIEW = "state_tag_recyclerview"

        private const val STAGGERED_SPAN_COUNT = 2
        private const val STAGGERED_SPAN_COUNT_MIN = 1
        private const val DELAY_SPAN: Long = 20
    }

    private val binding by viewBinding(DialogEditLayoutBinding::inflate)
    private val mainViewModel: MainViewModel by viewModels()

    private var spanCount = 2

    private lateinit var clip: Clip
    private lateinit var adapter: EditAdapter
    private lateinit var edType: EDType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppThemeHelper.applyDialogTheme(this)

        setContentView(binding.root)

        binding.toolbar.navigationIcon = drawableFrom(R.drawable.bubble_ic_cross)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener { saveClick() }

        setRecyclerView()

        edType = if (mainViewModel.editManager.getClip() != null) {

            /** Set the current clip for managing */

            clip = mainViewModel.editManager.getClip()!!

            binding.etMain.setText(clip.data)
            binding.tvBottomText.text = clip.getFullFormattedDate()

            EDType.Edit
        } else {
            EDType.Create
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        restoreData(savedInstanceState)

        /** A Timeout on binding creates a cool effect */

        lifecycleScope.launch {
            delay(DELAY_SPAN)
            bindUI()
        }
    }

    fun saveClick() {
        val text = binding.etMain.text.toString()

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
        mainViewModel.checkForDuplicateClip(text, clip.id, RepositoryListener(
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

        binding.recyclerView.adapter = adapter
    }

    private fun bindUI() {
        mainViewModel.editManager.spanCount.observe(this) { span ->
            val finalSpanCount = if (span == -1) STAGGERED_SPAN_COUNT else span
            refreshRecyclerView(finalSpanCount)
        }

        mainViewModel.editManager.tagFixedLiveData.observe(this) {
            if (it.size > 3)
                mainViewModel.editManager.postSpanCount(STAGGERED_SPAN_COUNT)
            else
                mainViewModel.editManager.postSpanCount(STAGGERED_SPAN_COUNT_MIN)
            adapter.submitList(it)
        }
    }

    private fun refreshRecyclerView(span: Int) {
        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.HORIZONTAL)
    }

    private fun restoreData(savedInstanceState: Bundle?) {
        val previousData = savedInstanceState?.getString(STATE_DIALOG_TEXT_FIELD)

        if (previousData?.isNotEmpty() == true)
            binding.etMain.setText(previousData)
    }

    override fun onStop() {
        val bundle = Bundle().apply {
            if (binding.etMain.text.length < 1000)
                putString(STATE_DIALOG_TEXT_FIELD, binding.etMain.text.toString())
            putParcelable(STATE_TAG_RECYCLERVIEW, binding.recyclerView.layoutManager?.onSaveInstanceState())
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