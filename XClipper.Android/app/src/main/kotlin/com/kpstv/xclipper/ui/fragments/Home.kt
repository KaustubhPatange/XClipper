package com.kpstv.xclipper.ui.fragments

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.runAutoSync
import com.kpstv.xclipper.App.swipeToDelete
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.enumerations.FirebaseState
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.recyclerview.RecyclerViewInsetHelper
import com.kpstv.xclipper.extensions.recyclerview.SwipeToDeleteCallback
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.registerForThemeChange
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.openAccessibility
import com.kpstv.xclipper.extensions.utils.Utils.Companion.shareText
import com.kpstv.xclipper.service.ChangeClipboardActivity
import com.kpstv.xclipper.ui.activities.NavViewModel
import com.kpstv.xclipper.ui.activities.Start
import com.kpstv.xclipper.ui.adapters.CIAdapter
import com.kpstv.xclipper.ui.dialogs.EditDialog
import com.kpstv.xclipper.ui.dialogs.TagDialog
import com.kpstv.xclipper.ui.fragments.sheets.MoreBottomSheet
import com.kpstv.xclipper.extensions.recyclerview.RecyclerViewScrollHelper
import com.kpstv.xclipper.ui.helpers.SyncDialogHelper
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_empty.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

@AndroidEntryPoint
class Home : ValueFragment(R.layout.fragment_home) {

    @Inject lateinit var clipboardProvider: ClipboardProvider
    @Inject lateinit var firebaseUtils: FirebaseUtils

    private lateinit var adapter: CIAdapter

    private val navViewModel by activityViewModels<NavViewModel>()
    private val mainViewModel: MainViewModel by viewModels()
    private val recyclerViewScrollHelper = RecyclerViewScrollHelper()
    private val swipeToDeleteItemTouch: ItemTouchHelper by lazy {
        ItemTouchHelper(
            SwipeToDeleteCallback(requireContext()) { pos ->
                mainViewModel.deleteFromRepository(adapter.getItemAt(pos))
                Toasty.info(requireContext(), getString(R.string.item_removed)).show()
            }
        )
    }

    override val forceBackPress: Boolean
        get() = mainViewModel.stateManager.isMultiSelectionStateActive() || searchView?.isSearchOpen == true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerForThemeChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()

        setToolbarCommonStuff()

        setSearchViewListener()

        setFloatingButton()

        setGoTopButton()

        emptyLayout.setOnClickListener(fabListener)

        bindUI()

        checkForAccessibilityService()

        checkClipboardData()

        autoValidateOnStartup()
    }

    override fun onBackPressed(): Boolean {
        when {
            mainViewModel.stateManager.isMultiSelectionStateActive() -> {
                mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)
                return true
            }
            searchView?.onBackPressed() == true -> return true
        }
        return super.onBackPressed()
    }

    private fun setGoTopButton() {
        btn_go_up.applyBottomInsets(merge = true)
        btn_go_up.setOnClickListener {
            recyclerViewScrollHelper.reset()
        }
    }

    private fun setFloatingButton() {
        fab_addItem.applyBottomInsets(merge = true)
        fab_addItem.setOnClickListener(fabListener)
    }

    private val fabListener = View.OnClickListener {
        val intent = Intent(requireContext(), EditDialog::class.java)
        startActivity(intent)
    }

    private fun bindUI() {
        mainViewModel.clipLiveData.observe(viewLifecycleOwner) { clips ->
            if (clips.isEmpty() && !mainViewModel.searchManager.anyFilterApplied())
                layout_empty_parent.show()
            else
                layout_empty_parent.collapse()
            if (clips != null) adapter.submitList(clips.cloneForAdapter())
            mainViewModel.stateManager.clearSelectedItem()
        }
        mainViewModel.stateManager.toolbarState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ToolbarState.NormalViewState -> {
                    fab_addItem.show()
                    setNormalToolbar()
                    mainViewModel.stateManager.clearSelectedList()
                    swipeToDeleteItemTouch.attachToRecyclerView(ci_recyclerView)
                }
                ToolbarState.MultiSelectionState -> {
                    setSelectedToolbar()
                    fab_addItem.hide()
                    swipeToDeleteItemTouch.attachToRecyclerView(null)
                }
                else -> {
                    // TODO: When exhaustive
                }
            }
        }
        mainViewModel.stateManager.selectedItemClips.observeForever { clips ->
            if (mainViewModel.stateManager.isMultiSelectionStateActive() && clips?.isEmpty() == true)
                mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)
        }
        mainViewModel.searchManager.tagFilters.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            updateTagFilters(it)
        }
    }

    private fun setRecyclerView() {
        adapter = CIAdapter(
            lifecycleOwner = viewLifecycleOwner,
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
                }
                CIAdapter.MENU_TYPE.Special -> {
                    MoreBottomSheet(
                        supportFragmentManager = childFragmentManager,
                        clip = clip
                    ).show(
                        childFragmentManager,
                        "blank"
                    )
                }
                CIAdapter.MENU_TYPE.Share -> {
                    shareText(requireActivity(), clip)
                }
            }
        }

        ci_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ci_recyclerView.recycledViewPool.setMaxRecycledViews(0, 15)
        ci_recyclerView.adapter = adapter

        /** Swipe to delete item */

        if (swipeToDelete)
            swipeToDeleteItemTouch.attachToRecyclerView(ci_recyclerView)

        RecyclerViewInsetHelper().attach(ci_recyclerView, RecyclerViewInsetHelper.InsetType.BOTTOM, true)
        recyclerViewScrollHelper.attach(
            ci_recyclerView,
            onScrollDown = {
                fab_addItem.hide()
                btn_go_up.animate().translationY(0f).start()
            },
            onScrollUp = {
                fab_addItem.show()
                btn_go_up.animate().translationY(500f).start()
            }
        )
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
                    startActivity(intent)
                }
            }
            true
        }

        mainViewModel.stateManager.selectedItemClips.observe(viewLifecycleOwner) {
            if (it.size > 0)
                toolbar.subtitle = "${it.size} ${getString(R.string.selected)}"
            else
                toolbar.subtitle = App.BLANK_STRING
        }
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

    private fun updateTagFilters(tags: List<Tag>) {
        ci_chip_group.removeAllViews()
        for (tag in tags) {
            ci_chip_group.addView(
                Chip(requireContext()).apply {
                    text = tag.name
                    setTag(App.TAG_FILTER_CHIP)
                    chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tag)
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
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
        appbarLayout.applyTopInsets()
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

                mainViewModel.makeASynchronizeRequest(StatusListener(
                    onBefore = {
                        isEnabled = true
                        this.clearAnimation()
                    },
                    onComplete = {
                        Toasty.info(requireContext(), getString(R.string.sync_complete)).show()
                    },
                    onError = {
                        val message = when (firebaseUtils.retrieveFirebaseStatus()) {
                            FirebaseState.NOT_INITIALIZED -> {
                                SyncDialogHelper.showDialog(requireContext())
                                getString(R.string.error_sync_uninitialized)
                            }
                            else -> {
                                getString(R.string.error_sync)
                            }
                        }
                        Toasty.error(requireContext(), message).show()
                    }
                ))
            }
        }

        val settingImage = LayoutInflater.from(requireContext()).inflate(R.layout.imageview_menu_setting, null) as ImageView
        settingImage.setOnClickListener {
            navViewModel.navigateTo(
                screen = Start.Screen.SETTING,
                animation = AnimationDefinition.Fade,
                transactionType = FragmentNavigator.TransactionType.ADD,
                addToBackStack = true,
            )
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

    override fun onDestroyView() {
        ci_recyclerView.adapter = null
        super.onDestroyView()
    }
}