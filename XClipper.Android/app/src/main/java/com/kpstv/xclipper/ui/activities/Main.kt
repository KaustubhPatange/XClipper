package com.kpstv.xclipper.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.BLANK_STRING
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.App.UNDO_DELETE_SPAN
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.Utils.Companion.shareText
import com.kpstv.xclipper.extensions.cloneForAdapter
import com.kpstv.xclipper.extensions.setOnQueryTextListener
import com.kpstv.xclipper.extensions.setOnSearchCloseListener
import com.kpstv.xclipper.ui.adapters.CIAdapter
import com.kpstv.xclipper.ui.helpers.MainEditHelper
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


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

        swipeRefreshLayout.isEnabled = false

        setRecyclerView()

        bindUI()

        checkClipboardData()

        setToolbarCommonStuff()

        setSearchViewListener()

        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
              val intent = Intent(
                  Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:$packageName")
              )
              startActivityForResult(intent, 0)
          }

        */
    }


    private fun bindUI() {
        mainViewModel.clipLiveData.observe(this, Observer {
            adapter.submitList(ArrayList(it?.cloneForAdapter()?.reversed()!!))
            mainViewModel.stateManager.clearSelectedItem()
            //  Log.e(TAG, "LiveData changed()")
        })
        mainViewModel.stateManager.toolbarState.observe(this, Observer { state ->
            when (state) {
                ToolbarState.NormalViewState -> {
                    setNormalToolbar()
                    mainViewModel.stateManager.clearSelectedList()
                }

                ToolbarState.MultiSelectionState -> {
                    setSelectedToolbar()
                }

                else -> {
                    // TODO: When exhaustive
                }
            }
        })
    }

    private fun setRecyclerView() {
        adapter = CIAdapter(
            context = this,
            selectedClips = mainViewModel.stateManager.selectedItemClips,
            selectedItem = mainViewModel.stateManager.selectedItem,
            multiSelectionState = mainViewModel.stateManager.multiSelectionState,
            onClick = { clip, _ ->
                if (mainViewModel.stateManager.isMultiSelectionStateActive())
                    mainViewModel.stateManager.addOrRemoveClipFromSelectedList(clip)
                else
                    mainViewModel.stateManager.addOrRemoveSelectedItem(clip)
                // expandMenuLogic(clip, pos)
            },
            onLongClick = { clip, _ ->
                mainViewModel.stateManager.clearSelectedItem()
                /*  adapter.list.forEach { it.toDisplay = false }
                  adapter.notifyDataSetChanged()*/

                mainViewModel.stateManager.setToolbarState(ToolbarState.MultiSelectionState)
                mainViewModel.stateManager.addOrRemoveClipFromSelectedList(clip)
            }
        )

        adapter.setCopyClick { clip, _ ->
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, clip.data?.Decrypt()))
            Toast.makeText(this, getString(R.string.ctc), Toast.LENGTH_SHORT).show()
        }

        adapter.setMenuItemClick { clip, i, menuType ->
            when (menuType) {
                CIAdapter.MENU_TYPE.Edit -> {
                    MainEditHelper(
                        this, mainViewModel
                    ).show(clip)
                }
                CIAdapter.MENU_TYPE.Delete -> {
                    performUndoDelete(clip, i)
                }
                CIAdapter.MENU_TYPE.Share -> {
                    shareText(this, clip)
                }
            }
        }

        ci_recyclerView.layoutManager = LinearLayoutManager(this)
        ci_recyclerView.adapter = adapter
        ci_recyclerView.setHasFixedSize(true)

    }

    /**
     * This will set the clicks of item on Toolbar Menu.
     */
    private fun setToolbarCommonStuff() {
        setNormalToolbar()
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_selectAll -> {
                    mainViewModel.stateManager.addAllToSelectedList(adapter.list)
                }
                R.id.action_selectNone -> {
                    mainViewModel.stateManager.clearSelectedList()
                }
                R.id.action_deleteAll -> {
                    deleteAllWithUndo()
                }
                R.id.action_search -> {
                    searchView.showSearch(true)
                }
            }
            true
        }


        //   Log.e(TAG, "Action View: ${}")
        //  val item = toolbar.menu.findItem(R.id.action_search)

        mainViewModel.stateManager.selectedItemClips.observe(this, Observer {
            if (it.size > 0)
                toolbar.subtitle = "${it.size} ${getString(R.string.selected)}"
            else
                toolbar.subtitle = BLANK_STRING
        })
    }

    private fun deleteAllWithUndo() {
        val totalItems = ArrayList(adapter.list)
        val itemsToRemove = mainViewModel.stateManager.selectedItemClips.value!!
        val size = itemsToRemove.size

        val task = Timer("UndoDelete", false).schedule(UNDO_DELETE_SPAN) {
            mainViewModel.deleteMultipleFromRepository(itemsToRemove)
        }

        if (size > 0)
            mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)

        adapter.list.removeAll(itemsToRemove)
        adapter.notifyDataSetChanged()

        Snackbar.make(
            ci_recyclerView,
            "$size ${getString(R.string.item_delete)}",
            Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.undo)) {
            task.cancel()
            adapter.list = totalItems
            adapter.notifyDataSetChanged()
        }.show()
    }


    private fun setSearchViewListener() {
        searchView.setOnQueryTextListener(
            onSubmit = { query ->
                ci_chip_group.addView(
                    Chip(this).apply {
                        text = query
                        isCloseIconVisible = true
                        setOnCloseIconClickListener { chip ->
                            ci_chip_group.removeView(chip)
                            mainViewModel.searchManager.addOrRemoveSearchFilter(query)
                        }
                    }
                )
                searchView.onBackPressed()
                mainViewModel.searchManager.clearSearch()
                mainViewModel.searchManager.addOrRemoveSearchFilter(query)
            },
            onChange = {
                mainViewModel.searchManager.setSearchText(it)
            },
            onClear = {
                mainViewModel.searchManager.clearSearch()
            }
        )
        searchView.setOnSearchCloseListener {
            mainViewModel.searchManager.clearSearch()
        }
    }

    /**
     * Call this function when ToolbarMultiSelection state is enabled.
     */
    private fun setSelectedToolbar() {
        toolbar.menu.clear()
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSelected))
        toolbar.inflateMenu(R.menu.selected_menu)
        toolbar.navigationIcon = getDrawable(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener {
            mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)
        }
    }

    /**
     * Call this function when ToolbarNormalState state is enabled.
     */
    private fun setNormalToolbar() {
        toolbar.navigationIcon = null
        toolbar.setNavigationOnClickListener(null)
        toolbar.menu.clear()
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        toolbar.inflateMenu(R.menu.normal_menu)

        val syncImage =
            LayoutInflater.from(this).inflate(R.layout.imageview_menu_item, null) as ImageView
        syncImage.apply {
            setOnClickListener {
                this.startAnimation(AnimationUtils.loadAnimation(this@Main, R.anim.rotate_clock)
                    .apply { repeatCount = Animation.INFINITE })
                isEnabled = false

                mainViewModel.makeAValidationRequest {
                    isEnabled = true
                    this.clearAnimation()
                    Toast.makeText(this@Main, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        toolbar.menu.findItem(R.id.action_sync).actionView = syncImage
    }


    /**
     * This function will perform undo delete whenever item has been deleted from
     * expanded menu.
     */
    private fun performUndoDelete(clip: Clip, i: Int) {
        val task = Timer("UndoDelete", false).schedule(UNDO_DELETE_SPAN) {
            mainViewModel.deleteFromRepository(clip)
        }

        val list = adapter.list.removeAt(i)
        adapter.notifyItemRemoved(i)

        Snackbar.make(
            ci_recyclerView,
            "1 ${getString(R.string.item_delete)}",
            Snackbar.LENGTH_SHORT
        )
            .setAction(getString(R.string.undo)) {
                task.cancel()
                adapter.list.add(i, list)
                adapter.notifyItemInserted(i)
            }.show()
    }


    /**
     * So I found out that sometimes in Android 10, clipboard still not get captured using my
     * accessibility service hack. To fix this whenever app is launched or come back from
     * background it will check & update the database with the clipboard.
     */
    private fun checkClipboardData() {
        val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        if (data != null && CLIP_DATA != data) {
            CLIP_DATA = data

            mainViewModel.postToRepository(data)

            Log.e(TAG, "Pushed: $data")
        }
    }


    /**
     * This function will handle the expanded menu logic
     */
    private fun expandMenuLogic(model: Clip, pos: Int) {
        for ((i, e) in adapter.list.withIndex()) {
            if (i != pos && e.toDisplay) {
                e.toDisplay = false
                adapter.notifyItemChanged(i)
            }
        }
        model.toDisplay = !model.toDisplay
        adapter.notifyItemChanged(pos)
    }

    override fun onBackPressed() {
        when {
            mainViewModel.stateManager.isMultiSelectionStateActive() -> mainViewModel.stateManager.setToolbarState(
                ToolbarState.NormalViewState
            )
            searchView.onBackPressed() -> return
            else -> super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkClipboardData()
    }
}