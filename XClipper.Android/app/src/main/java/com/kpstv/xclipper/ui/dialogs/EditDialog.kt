package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.kpstv.license.Decrypt
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.ui.adapters.EditAdapter
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_edit_layout.*
import kotlinx.coroutines.delay
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class EditDialog : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val viewModelFactory: MainViewModelFactory by instance()

    private lateinit var clip: Clip
    private lateinit var adapter: EditAdapter
    private lateinit var edType: EDType
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_layout)

        toolbar.navigationIcon = getDrawable(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        setRecyclerView()

        edType = if (mainViewModel.editManager.getClip() == null) EDType.Create
        else {

            /** Set the current clip for managing */
            clip = mainViewModel.editManager.getClip()!!

            de_editText.setText(clip.data?.Decrypt())
            EDType.Edit
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

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
                mainViewModel.postUpdateToRepository(
                    clip,
                    /** In the second parameter we are also supplying the tags as well. */
                    clip.clone(text.Encrypt(), mainViewModel.editManager.getSelectedTags())
                )
                postSuccess()
            } else {
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
        } else
            Toasty.error(this, getString(R.string.error_empty_text)).show()
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

        del_recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        del_recyclerView.adapter = adapter
    }

    private fun bindUI() {
        mainViewModel.editManager.tagFixedLiveData.observe(this, Observer {
            adapter.submitList(it)
        })
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