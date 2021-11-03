package com.kpstv.xclipper.ui.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.xclipper.App.runAutoSync
import com.kpstv.xclipper.App.swipeToDelete
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.FragmentHomeBinding
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.enumerations.FirebaseState
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.recyclerview.RecyclerViewInsetHelper
import com.kpstv.xclipper.extensions.recyclerview.SwipeToDeleteCallback
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import com.kpstv.xclipper.ui.helpers.AppThemeHelper.Companion.registerForThemeChange
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
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.ui.helpers.fragments.SyncDialogHelper
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.zhuinden.livedatacombinetuplekt.combineTuple
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class Home : ValueFragment(R.layout.fragment_home) {

    @Inject lateinit var clipboardProvider: ClipboardProvider
    @Inject lateinit var preferenceProvider: PreferenceProvider
    @Inject lateinit var firebaseUtils: FirebaseUtils

    private lateinit var adapter: CIAdapter
    private var undoSnackBar: Snackbar? = null

    private val binding by viewBinding(FragmentHomeBinding::bind)

    private val navViewModel by activityViewModels<NavViewModel>()
    private val mainViewModel: MainViewModel by viewModels()
    private val recyclerViewScrollHelper = RecyclerViewScrollHelper()
    private val swipeToDeleteItemTouch: ItemTouchHelper by lazy {
        ItemTouchHelper(
            SwipeToDeleteCallback(requireContext()) { pos ->
                val item = adapter.getItemAt(pos)
                showUndoAndDelete(listOf(item))
            }
        )
    }

    override val forceBackPress: Boolean
        get() = mainViewModel.stateManager.isMultiSelectionStateActive() || binding.searchView.isSearchOpen || mainViewModel.searchManager.anyFilterApplied()

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

        binding.layoutEmptyParent.emptyLayout.setOnClickListener(fabListener)

        bindUI()

        checkForAccessibilityService()

        checkClipboardData()

        autoValidateOnStartup()

        attachTouchEventListenerRecursively(view)
    }

    override fun onBackPressed(): Boolean {
        when {
            mainViewModel.stateManager.isMultiSelectionStateActive() -> {
                mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)
                return true
            }
            binding.searchView.onBackPressed() -> return true
            mainViewModel.searchManager.anyFilterApplied() -> {
                mainViewModel.searchManager.clearAll()
                return true
            }
        }
        return super.onBackPressed()
    }

    private fun setGoTopButton() {
        binding.btnGoUp.applyBottomInsets(merge = true)
        binding.btnGoUp.setOnClickListener {
            recyclerViewScrollHelper.reset()
        }
    }

    private fun setFloatingButton() {
        binding.fabAddItem.applyBottomInsets(merge = true)
        binding.fabAddItem.setOnClickListener(fabListener)
    }

    private val fabListener = View.OnClickListener {
        val intent = Intent(requireContext(), EditDialog::class.java)
        startActivity(intent)
    }

    private fun bindUI() {
        mainViewModel.clipLiveData.observe(viewLifecycleOwner) { clips ->
            if (clips?.isEmpty() == true && !mainViewModel.searchManager.anyFilterApplied())
                binding.layoutEmptyParent.root.show()
            else
                binding.layoutEmptyParent.root.collapse()
            if (clips != null) adapter.submitList(clips)
            mainViewModel.stateManager.clearSelectedItem()
        }
        mainViewModel.stateManager.toolbarState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ToolbarState.NormalViewState -> {
                    binding.fabAddItem.show()
                    setNormalToolbar()
                    mainViewModel.stateManager.clearSelectedList()
                    swipeToDeleteItemTouch.attachToRecyclerView(binding.ciRecyclerView)
                }
                ToolbarState.MultiSelectionState -> {
                    setSelectedToolbar()
                    binding.fabAddItem.hide()
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
        combineTuple(mainViewModel.searchManager.searchFilters, mainViewModel.searchManager.tagFilters)
            .observe(viewLifecycleOwner) { (searchFilters: ArrayList<String>?, tagFilters: ArrayList<Tag>?) ->
                if (searchFilters == null || tagFilters == null) return@observe
                updateSearchAndTagFilters(searchFilters, tagFilters)
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
                    MoreBottomSheet.show(
                        fragment = this,
                        clip = clip,
                        preferenceProvider = preferenceProvider
                    )
                }
                CIAdapter.MENU_TYPE.Share -> {
                    shareText(requireActivity(), clip)
                }
            }
        }

        with(binding) {
            ciRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            ciRecyclerView.recycledViewPool.setMaxRecycledViews(0, 15)
            ciRecyclerView.adapter = adapter

            /** Swipe to delete item */

            if (swipeToDelete)
                swipeToDeleteItemTouch.attachToRecyclerView(ciRecyclerView)

            RecyclerViewInsetHelper().attach(ciRecyclerView, RecyclerViewInsetHelper.InsetType.BOTTOM, true)
            recyclerViewScrollHelper.attach(
                ciRecyclerView,
                onScrollDown = {
                    binding.fabAddItem.hide()
                    binding.btnGoUp.animate().translationY(0f).start()
                },
                onScrollUp = {
                    binding.fabAddItem.show()
                    binding.btnGoUp.animate().translationY(500f).start()
                }
            )
        }
    }

    /**
     * This will set the clicks of item on Toolbar Menu.
     */
    private fun setToolbarCommonStuff() {
        setNormalToolbar()
        binding.toolbar.setOnMenuItemClickListener { item ->
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
                    binding.searchView.showSearch(true)
                }
                R.id.action_tag -> {
                    val intent = Intent(requireContext(), TagDialog::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        mainViewModel.stateManager.selectedItemClips.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                binding.toolbar.subtitle = "${it.size} ${getString(R.string.selected)}"
            else
                binding.toolbar.subtitle = BLANK_STRING // requires to show empty subtitle
        }
    }

    private fun deleteAllWithUndo() {
        val itemsToRemove = mainViewModel.stateManager.selectedItemClips.value!!

        if (itemsToRemove.isNotEmpty())
            mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)

        showUndoAndDelete(itemsToRemove)
    }

    private fun showUndoAndDelete(itemsToRemove: List<Clip>) {
        undoSnackBar?.dismiss() // clear previous snackbar

        val clonedItems = ArrayList(adapter.currentList)
        val tweakItems = ArrayList(adapter.currentList)

        tweakItems.removeAll(itemsToRemove)
        adapter.submitList(tweakItems)

        val snackbarCallback = object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                mainViewModel.deleteMultipleFromRepository(itemsToRemove)
            }
        }

        Snackbar.make(
            binding.ciRecyclerView,
            "${itemsToRemove.size} ${getString(R.string.item_delete)}",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.undo)) {
                removeCallback(snackbarCallback)
                adapter.submitList(clonedItems)
                adapter.notifyDataSetChanged()
            }
            addCallback(snackbarCallback)
            show()
            undoSnackBar = this
        }
    }

    private fun setSearchViewListener() = with(binding) {
        searchView.setOnQueryTextListener(
            onSubmit = { query ->
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
        searchView.setOnSearchViewListener(object : SimpleSearchView.SearchViewListener by DelegatedSearchViewListener {
            override fun onSearchViewClosed() {
                mainViewModel.searchManager.clearSearch()
            }
        })
    }

    private fun updateSearchAndTagFilters(searches: List<String>, tags: List<Tag>) {
        binding.ciChipGroup.removeAllViews()
        searches.forEach { query ->
            binding.ciChipGroup.addView(
                Chip(requireContext()).apply {
                    text = query
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        mainViewModel.searchManager.addOrRemoveSearchFilter(query)
                    }
                }
            )
        }
        for (tag in tags) {
            binding.ciChipGroup.addView(
                Chip(requireContext()).apply {
                    text = tag.name
                    setTag(TAG_FILTER_CHIP)
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
    private fun setSelectedToolbar() = with(binding) {
        toolbar.menu.clear()
        toolbar.setBackgroundColor(AppThemeHelper.CARD_SELECTED_COLOR)
        toolbar.inflateMenu(R.menu.selected_menu)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
        toolbar.setNavigationOnClickListener {
            mainViewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)
        }
    }


    /**
     * Call this function when ToolbarNormalState state is enabled.
     */
    private fun setNormalToolbar() = with(binding) {
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
                remember = true,
            )
        }

        toolbar.menu.findItem(R.id.action_sync).actionView = syncImage
        toolbar.menu.findItem(R.id.action_setting).actionView = settingImage
    }

    /**
     * This will automatically trigger data validation by clicking on toolbar
     * sync button.
     */
    private fun autoValidateOnStartup() = with(binding) {
        if (runAutoSync)
            toolbar.menu.findItem(R.id.action_sync).actionView?.performClick()
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { _, event ->
        if (event.actionMasked == MotionEvent.ACTION_DOWN && undoSnackBar != null) {
            undoSnackBar?.dismiss()
            undoSnackBar = null
        }
        return@OnTouchListener false
    }

    private fun attachTouchEventListenerRecursively(view: View) {
        if (view !is TextView)
            view.setOnTouchListener(onTouchListener)
        if (view is ViewGroup) {
            view.children.forEach loop@ { child ->
                if (child is RecyclerView) {
                    child.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                            return onTouchListener.onTouch(rv, e)
                        }
                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                    })
                    return@loop
                }
                attachTouchEventListenerRecursively(child)
            }
        }
    }

    /**
     * This will show a small snackbar if our clipboard accessibility service is disabled.
     */
    private fun checkForAccessibilityService() {
        if (!ClipboardAccessibilityService.isRunning(requireContext())) {
            Snackbar.make(
                binding.ciRecyclerView,
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

    private companion object {
        private const val BLANK_STRING = " "
        private const val TAG_FILTER_CHIP = "com.kpstv.xclipper.tag"

    }
}