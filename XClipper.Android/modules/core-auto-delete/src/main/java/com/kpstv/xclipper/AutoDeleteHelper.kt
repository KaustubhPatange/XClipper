package com.kpstv.xclipper

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.ui.sheet.AutoDeleteBottomSheet

object AutoDeleteHelper {
    fun isEnabled(context: Context): Boolean =
        CommonReusableEntryPoints.get(context).appSettings().canAutoDeleteClips()

    fun showConfigSheet(fragmentManager: FragmentManager) {
        val sheet = AutoDeleteBottomSheet()
        sheet.show(fragmentManager, "auto-delete")
    }

    fun reset(context: Context) {
        CommonReusableEntryPoints.get(context).appSettings().setAutoDeleteClips(false)
    }
}