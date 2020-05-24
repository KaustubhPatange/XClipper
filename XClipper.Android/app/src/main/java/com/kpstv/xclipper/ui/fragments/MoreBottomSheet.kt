package com.kpstv.xclipper.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.ui.viewmodels.MainViewModel


class MoreBottomSheet(
    private val mainViewModel: MainViewModel
): RoundedBottomSheetDialogFragment() {
    private val TAG = javaClass.simpleName
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_more, container, false)

        mainViewModel.tinyUrlApiHelper.createShortenUrl("example.com", ResponseListener(
            complete = {
                Log.e(TAG, "Shorten: $it")
            },
            error = {
                Log.e(TAG, "Error: ${it.message}")
            }
        ))

        return view
    }
}