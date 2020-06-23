package com.kpstv.xclipper.ui.fragments

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.UPDATE_REQUEST_CODE
import com.kpstv.xclipper.App.runAutoSync
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.openAccessibility
import com.kpstv.xclipper.extensions.utils.Utils.Companion.shareText
import com.kpstv.xclipper.service.ChangeClipboardActivity
import com.kpstv.xclipper.ui.activities.Settings
import com.kpstv.xclipper.ui.adapters.CIAdapter
import com.kpstv.xclipper.ui.dialogs.EditDialog
import com.kpstv.xclipper.ui.dialogs.TagDialog
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class Home : Fragment(R.layout.fragment_home), KodeinAware {

    override val kodein by kodein()
    private val viewModelFactory by instance<MainViewModelFactory>()
    private val clipboardProvider by instance<ClipboardProvider>()

    private lateinit var adapter: CIAdapter

    private lateinit var mainViewModel: MainViewModel
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        super.onCreate(savedInstanceState)
    }

    private val TAG = javaClass.simpleName
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)

        setRecyclerView()

        setToolbarCommonStuff()

        setSearchViewListener()

        fab_addItem.setOnClickListener(fabListener)

        emptyLayout.setOnClickListener(fabListener)

        bindUI()

        checkForAccessibilityService()

        checkClipboardData()

        /** Load app list in GlobalScope */
        GlobalScope.launch {
            Utils.retrievePackageList(requireContext())
        }

        autoValidateOnStartup()

        checkForUpdates()

        super.onViewCreated(view, savedInstanceState)
    }

    private val fabListener = View.OnClickListener {
        val intent = Intent(requireContext(), EditDialog::class.java)
        startActivity(intent)
    }

    private fun checkForUpdates() = with(requireActivity()) {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateManager.registerListener(listener)
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            Log.e(TAG, "Bytes Downloaded: $bytesDownloaded, TotalBytes: $totalBytesToDownload")
        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                requireView().findViewById(R.id.ci_recyclerView),
                "An update has just been downloaded.",
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction("RESTART") { appUpdateManager.completeUpdate() }
                show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == App.TAG_DIALOG_REQUEST_CODE && resultCode == App.TAG_DIALOG_RESULT_CODE) {
            tagDialogItemClickListener(mainViewModel.getTag()!!)
        }
    }

    private fun bindUI() {
        mainViewModel.clipLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                layout_empty_parent.show()
            else
                layout_empty_parent.collapse()
            adapter.submitList(ArrayList(it?.cloneForAdapter()?.reversed()!!))
            mainViewModel.stateManager.clearSelectedItem()
        })
        mainViewModel.stateManager.toolbarState.observe(viewLifecycleOwner, Observer { state ->
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
            context = requireContext(),
            selectedClips = mainViewModel.stateManager.selectedItemClips,
            selectedItem = mainViewModel.stateManager.selectedItem,
            currentClip = mainViewModel.currentClip,
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
            clipboardProvider.setClipboard(ClipData.newPlainText(null, clip.data))
            Toasty.info(requireContext(), getString(R.string.ctc)).show()
        }

        adapter.setMenuItemClick { clip, _, menuType ->
            when (menuType) {
                CIAdapter.MENU_TYPE.Edit -> {
                    /** This will ensure that we are editing the clip */
                    mainViewModel.editManager.postClip(clip)

                    val intent = Intent(requireContext(), EditDialog::class.java)
                    startActivity(intent)
                }
                CIAdapter.MENU_TYPE.Pin -> {
                    mainViewModel.changeClipPin(clip, !clip.isPinned)

                    /*Toasty.info(requireContext(), "Clip updated").show()*/
                }
                CIAdapter.MENU_TYPE.Special -> {
                    MoreBottomSheet(
                        tinyUrlApiHelper = mainViewModel.tinyUrlApiHelper,
                        dictionaryApiHelper = mainViewModel.dictionaryApiHelper,
                        supportFragmentManager = requireActivity().supportFragmentManager,
                        clip = clip
                    ).show(
                        requireActivity().supportFragmentManager,
                        "blank"
                    )
                }
                CIAdapter.MENU_TYPE.Share -> {
                    shareText(requireActivity(), clip)
                }
            }
        }

        ci_recyclerView.layoutManager = LinearLayoutManager(requireContext())
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
                    val intent = Intent(requireContext(), TagDialog::class.java)
                    startActivityForResult(intent, App.TAG_DIALOG_REQUEST_CODE)
                }
            }
            true
        }


        //   Log.e(TAG, "Action View: ${}")
        //  val item = toolbar.menu.findItem(R.id.action_search)

        mainViewModel.stateManager.selectedItemClips.observe(viewLifecycleOwner, Observer {
            if (it.size > 0)
                toolbar.subtitle = "${it.size} ${getString(R.string.selected)}"
            else
                toolbar.subtitle = App.BLANK_STRING
        })
    }

    private fun deleteAllWithUndo() {
        val totalItems = ArrayList(adapter.currentList)
        val tweakItems = ArrayList(totalItems)
        val itemsToRemove = mainViewModel.stateManager.selectedItemClips.value!!
        val size = itemsToRemove.size

        val task = Timer("UndoDelete", false).schedule(App.UNDO_DELETE_SPAN) {
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
                    Chip(requireContext()).apply {
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
                    view.tag == App.TAG_FILTER_CHIP && view.text == tag.name
                else
                    false
            } <= 0) {
            ci_chip_group.addView(
                Chip(requireContext()).apply {
                    text = tag.name
                    setTag(App.TAG_FILTER_CHIP)
                    chipIcon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_tag)
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
        toolbar.setBackgroundColor(ThemeUtils.CARD_SELECTED_COLOR)
        toolbar.inflateMenu(R.menu.selected_menu)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
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
        toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        toolbar.inflateMenu(R.menu.normal_menu)

        val syncImage =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.imageview_menu_item, null) as ImageView

        syncImage.apply {
            setOnClickListener {
                this.startAnimation(
                    AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_clock)
                        .apply { repeatCount = Animation.INFINITE })
                isEnabled = false

                mainViewModel.makeAValidationRequest(StatusListener(
                    onBefore = {
                        isEnabled = true
                        this.clearAnimation()
                    },
                    onComplete = {
                        Toasty.info(requireContext(), getString(R.string.sync_complete)).show()
                    },
                    onError = {
                        Toasty.error(requireContext(), getString(R.string.error_sync)).show()
                    }
                ))
            }
        }

        val settingImage =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.imageview_menu_setting, null) as ImageView
        settingImage.setOnClickListener {
            startActivity(Intent(requireContext(), Settings::class.java))
        }

        toolbar.menu.findItem(R.id.action_sync).actionView = syncImage
        toolbar.menu.findItem(R.id.action_setting).actionView = settingImage
    }

    /**
     * This will automatically trigger data validation by clicking on toolbar
     * sync button.
     */
    private fun autoValidateOnStartup() {
        if (runAutoSync)
            toolbar.menu.findItem(R.id.action_sync).actionView?.performClick()
    }


    /**
     * This will show a small snackbar if our clipboard accessibility service is disabled.
     */
    private fun checkForAccessibilityService() {
        if (!Utils.isClipboardAccessibilityServiceRunning(requireContext())) {
            Snackbar.make(
                ci_recyclerView,
                "Accessibility service not running",
                Snackbar.LENGTH_LONG
            )
                .setAction("Enable") {
                    openAccessibility(requireContext())
                }.show()
        }
    }


    /**
     * So I found out that sometimes in Android 10, clipboard still not get captured using the
     * accessibility service hack. To fix this whenever app is launched or come back from
     * background it will check & update the database with the clipboard.
     */
    private fun checkClipboardData() {
        val intent = Intent(requireContext(), ChangeClipboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        }
        startActivity(intent)
    }

}