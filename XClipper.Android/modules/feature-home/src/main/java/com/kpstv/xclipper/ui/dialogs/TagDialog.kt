package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.extension.enumeration.DialogState
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_home.R
import com.kpstv.xclipper.feature_home.databinding.DialogCreateTagBinding
import com.kpstv.xclipper.ui.adapter.TagAdapter
import com.kpstv.xclipper.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class TagDialog : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    private val binding by viewBinding(DialogCreateTagBinding::inflate)
    private val mainViewModel by viewModels<MainViewModel>()

    private lateinit var adapter: TagAdapter
    private lateinit var switchCompat: SwitchCompat


    companion object {
        private const val DELAY_SPAN: Long = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppThemeHelper.applyDialogTheme(this)

        setContentView(binding.root)

        setToolbar()

        setRecyclerView()

        binding.etCreate.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendButton()
                    return true
                }
                return false
            }

        })
        binding.btnSend.setOnClickListener {
            sendButton()
        }

        lifecycleScope.launchWhenStarted {
            delay(DELAY_SPAN)
            bindUI()
            binding.lvTagContainer.minimumHeight = resources.getDimension(R.dimen.dimen170).toInt()
        }
    }

    private fun sendButton() {
        if (binding.etCreate.text.isNotBlank()) {
            mainViewModel.postToTagRepository(Tag.from(binding.etCreate.text.trim().toString()))
            binding.etCreate.text.clear()
        }
    }

    private fun bindUI() {
        mainViewModel.tagLiveData.observe(this) {
            if (it.isEmpty()) mainViewModel.stateManager.setDialogState(DialogState.Edit)
            adapter.submitList(it)
        }

        mainViewModel.stateManager.dialogState.observe(this) { state ->
            when (state) {
                DialogState.Normal -> {
                    binding.tvFilterTags.text = getString(R.string.custom_tags)
                    binding.etCreate.text.clear()
                    binding.editLayout.collapse()
                    switchCompat.isChecked = false
                }
                DialogState.Edit -> {
                    binding.tvFilterTags.text = getString(R.string.add_remove_tags)
                    binding.editLayout.isEnabled = true
                    binding.editLayout.show()
                    switchCompat.isChecked = true
                    binding.etCreate.requestFocus()
                }
                else -> {
                    // When exhaustive
                }
            }
        }
    }

    private fun setToolbar() = with(binding) {
        toolbar.setNavigationIcon(R.drawable.ic_cross)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        toolbar.inflateMenu(R.menu.dct_menu)

        switchCompat =
            LayoutInflater.from(this@TagDialog).inflate(R.layout.switch_layout, null) as SwitchCompat
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
        binding.recyclerView.layoutManager = layoutManager

        adapter = TagAdapter(
            lifecycleOwner = this,
            dialogState = mainViewModel.stateManager.dialogState,
            tagFilter = mainViewModel.searchManager.tagFilters,
            tagMapData = mainViewModel.tagMapData,
            onCloseClick = { tag, count, _ ->
                if (count > 0) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.tag_delete_title, tag.name))
                        .setMessage(getString(R.string.tag_delete_message, count))
                        .setPositiveButton(R.string.ok) { _, _ ->
                            mainViewModel.deleteFromTagRepository(tag)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                } else {
                    mainViewModel.deleteFromTagRepository(tag)
                }
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
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        mainViewModel.stateManager.setDialogState(DialogState.Normal)
        super.onDestroy()
    }
}