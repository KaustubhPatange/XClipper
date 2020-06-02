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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.BLANK_STRING
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.App.TAG_DIALOG_REQUEST_CODE
import com.kpstv.xclipper.App.TAG_DIALOG_RESULT_CODE
import com.kpstv.xclipper.App.TAG_FILTER_CHIP
import com.kpstv.xclipper.App.UNDO_DELETE_SPAN
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.cloneForAdapter
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.setOnQueryTextListener
import com.kpstv.xclipper.extensions.setOnSearchCloseListener
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_SELECTED_COLOR
import com.kpstv.xclipper.extensions.utils.Utils.Companion.shareText
import com.kpstv.xclipper.ui.adapters.CIAdapter
import com.kpstv.xclipper.ui.dialogs.EditDialog
import com.kpstv.xclipper.ui.dialogs.TagDialog
import com.kpstv.xclipper.ui.fragments.MoreBottomSheet
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import es.dmoral.toasty.Toasty
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
    private val viewModelFactory by instance<MainViewModelFactory>()

    //  private lateinit var tagDialog: TagDialog

    private val clipboardManager: ClipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    var isDarkTheme = true

    private lateinit var adapter: CIAdapter

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isDarkTheme = DARK_THEME

        ThemeUtils.setTheme(this)

        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        setContentView(R.layout.activity_main)

        setRecyclerView()

        checkClipboardData()

        setToolbarCommonStuff()

        setSearchViewListener()

        fab_addItem.setOnClickListener {
            val intent = Intent(this, EditDialog::class.java)
            startActivity(intent)
        }

        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
              val intent = Intent(
                  Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:$packageName")
              )
              startActivityForResult(intent, 0)
          }

        */
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        bindUI()
    }

    /**
     * Check if current theme has changed...
     */
    override fun onResume() {
        super.onResume()
        if (DARK_THEME != isDarkTheme) {
            val previousIntent = intent
            finish()
            startActivity(previousIntent);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAG_DIALOG_REQUEST_CODE && resultCode == TAG_DIALOG_RESULT_CODE) {
            tagDialogItemClickListener(mainViewModel.getTag()!!)
        }
    }


    private fun bindUI() {
        mainViewModel.clipLiveData.observe(this, Observer {
            adapter.submitList(ArrayList(it?.cloneForAdapter()?.reversed()!!))
            mainViewModel.stateManager.clearSelectedItem()
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
            },
            onLongClick = { clip, _ ->
                mainViewModel.stateManager.clearSelectedItem()
                mainViewModel.stateManager.setToolbarState(ToolbarState.MultiSelectionState)
                mainViewModel.stateManager.addOrRemoveClipFromSelectedList(clip)
            }
        )

        adapter.setCopyClick { clip, _ ->
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, clip.data?.Decrypt()))
            Toasty.info(this, getString(R.string.ctc)).show()
        }

        adapter.setMenuItemClick { clip, _, menuType ->
            when (menuType) {
                CIAdapter.MENU_TYPE.Edit -> {
                    /** This will ensure that we are editing the clip */
                    mainViewModel.editManager.postClip(clip)

                    val intent = Intent(this, EditDialog::class.java)
                    startActivity(intent)
                }
                CIAdapter.MENU_TYPE.Special -> {
                    MoreBottomSheet(
                        tinyUrlApiHelper = mainViewModel.tinyUrlApiHelper,
                        dictionaryApiHelper = mainViewModel.dictionaryApiHelper,
                        supportFragmentManager = supportFragmentManager,
                        clip = clip
                    ).show(
                        supportFragmentManager,
                        "blank"
                    )
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
                    mainViewModel.stateManager.addAllToSelectedList(ArrayList(adapter.currentList))
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
                R.id.action_tag -> {
                    val intent = Intent(this, TagDialog::class.java)
                    startActivityForResult(intent, TAG_DIALOG_REQUEST_CODE)
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
        val totalItems = ArrayList(adapter.currentList)
        val tweakItems = ArrayList(totalItems)
        val itemsToRemove = mainViewModel.stateManager.selectedItemClips.value!!
        val size = itemsToRemove.size

        val task = Timer("UndoDelete", false).schedule(UNDO_DELETE_SPAN) {
            mainViewModel.deleteMultipleFromRepository(itemsToRemove)
        }

        if (size > 0)
            mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)

        tweakItems.removeAll(itemsToRemove)
        adapter.submitList(tweakItems)

        Snackbar.make(
            ci_recyclerView,
            "$size ${getString(R.string.item_delete)}",
            Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.undo)) {
            task.cancel()
            adapter.submitList(totalItems)
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
     * This must be invoke when item on filter dialog is clicked.
     *
     * Right now it is captured through onActivityResult of TipDialog.
     */
    private fun tagDialogItemClickListener(tag: Tag) {
        if (ci_chip_group.children.count { view ->
                if (view is Chip)
                    view.tag == TAG_FILTER_CHIP && view.text == tag.name
                else
                    false
            } <= 0) {
            ci_chip_group.addView(
                Chip(this).apply {
                    text = tag.name
                    setTag(TAG_FILTER_CHIP)
                    chipIcon =
                        ContextCompat.getDrawable(this@Main, R.drawable.ic_tag)
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { chip ->
                        ci_chip_group.removeView(chip)
                        mainViewModel.searchManager.removeTagFilter(tag)
                    }
                }
            )
        }
    }

    /**
     * Call this function when ToolbarMultiSelection state is enabled.
     */
    private fun setSelectedToolbar() {
        toolbar.menu.clear()
        toolbar.setBackgroundColor(CARD_SELECTED_COLOR)
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

                mainViewModel.makeAValidationRequest(StatusListener(
                    onBefore = {
                        isEnabled = true
                        this.clearAnimation()
                    },
                    onComplete = {
                        Toasty.info(this@Main, getString(R.string.sync_complete)).show()
                    },
                    onError = {
                        Toasty.error(this@Main, getString(R.string.error_sync)).show()
                    }
                ))
            }
        }

        val settingImage =
            LayoutInflater.from(this).inflate(R.layout.imageview_menu_setting, null) as ImageView
        settingImage.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

        toolbar.menu.findItem(R.id.action_sync).actionView = syncImage
        toolbar.menu.findItem(R.id.action_setting).actionView = settingImage
    }

    /**
     * So I found out that sometimes in Android 10, clipboard still not get captured using the
     * accessibility service hack. To fix this whenever app is launched or come back from
     * background it will check & update the database with the clipboard.
     */
    private fun checkClipboardData() {
        val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        if (!data.isNullOrBlank() && CLIP_DATA != data) {
            CLIP_DATA = data

            mainViewModel.postToRepository(data)

            Log.e(TAG, "Pushed: $data")
        }
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