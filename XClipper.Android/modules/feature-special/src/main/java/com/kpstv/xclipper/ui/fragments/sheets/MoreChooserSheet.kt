package com.kpstv.xclipper.ui.fragments.sheets

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_special.R
import com.kpstv.xclipper.feature_special.databinding.BottomSheetSpecialChooserBinding
import com.kpstv.xclipper.ui.adapters.MoreChooserAdapter

internal class MoreChooserSheet(
    private val items: List<String>,
    private val onItemSelected: (String) -> Unit
) : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_special_chooser) {

    private val binding: BottomSheetSpecialChooserBinding by viewBinding(BottomSheetSpecialChooserBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = MoreChooserAdapter(items) {
            onItemSelected.invoke(it)
            dismiss()
        }
        binding.recyclerView.setHasFixedSize(true)
    }
}