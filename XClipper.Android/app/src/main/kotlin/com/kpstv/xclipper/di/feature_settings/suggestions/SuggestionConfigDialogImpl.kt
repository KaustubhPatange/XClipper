package com.kpstv.xclipper.di.feature_settings.suggestions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kpstv.xclipper.di.suggestions.SuggestionConfigDialog
import com.kpstv.xclipper.ui.dialogs.SuggestionDialogFragment
import javax.inject.Inject

class SuggestionConfigDialogImpl @Inject constructor(
//    private val fragment: Fragment
) : SuggestionConfigDialog {

    override fun show(fragmentManager: FragmentManager) {
        val dialog = SuggestionDialogFragment()
        dialog.showNow(fragmentManager, "suggest")
    }
}