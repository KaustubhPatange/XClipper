/*
 *   ColorSheet
 *
 *   Copyright (c) 2019. Sasikanth Miriyampalli
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package dev.sasikanth.colorsheet

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sasikanth.colorsheet.utils.Theme
import dev.sasikanth.colorsheet.utils.resolveColor

/**
 * Listener for color picker
 *
 * returns color selected from the sheet. If noColorOption is enabled and user selects the option,
 * it will return [ColorSheet.NO_COLOR]
 */
typealias ColorPickerListener = ((color: Int) -> Unit)?

@Suppress("unused")
open class ColorSheet : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "ColorSheet"
        const val NO_COLOR = -1
    }

    private var sheetCorners: Float = 0f
    private var colorAdapter: ColorAdapter? = null

    override fun getTheme(): Int {
        return Theme.inferTheme(requireContext()).styleRes
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null) dismiss()
        return inflater.inflate(R.layout.color_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog? ?: return
                val behavior = dialog.behavior
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = 0
                behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            dismiss()
                        }
                    }
                })
            }
        })

        if (sheetCorners == 0f) {
            sheetCorners = resources.getDimension(R.dimen.default_dialog_radius)
        }

        val gradientDrawable = GradientDrawable().apply {
            if (Theme.inferTheme(requireContext()) == Theme.LIGHT) {
                setColor(resolveColor(requireContext(), colorRes = R.color.dialogPrimary))
            } else {
                setColor(resolveColor(requireContext(), colorRes = R.color.dialogDarkPrimary))
            }

            cornerRadii =
                floatArrayOf(sheetCorners, sheetCorners, sheetCorners, sheetCorners, 0f, 0f, 0f, 0f)
        }
        view.background = gradientDrawable

        if (colorAdapter != null) {
            view.findViewById<RecyclerView>(R.id.colorSheetList).adapter = colorAdapter
        }

        view.findViewById<ImageView>(R.id.colorSheetClose).setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        colorAdapter = null
    }

    /**
     * Set corner radius of sheet top left and right corners.
     *
     * @param radius: Takes a float value
     */
    fun cornerRadius(radius: Float): ColorSheet {
        this.sheetCorners = radius
        return this
    }

    /**
     * Set corner radius of sheet top left and right corners.
     *
     * @param radius: Takes a float value
     */
    fun cornerRadius(radius: Int): ColorSheet {
        return cornerRadius(radius.toFloat())
    }

    /**
     * Config color picker
     *
     * @param colors: Array of colors to show in color picker
     * @param selectedColor: Pass in the selected color from colors list, default value is null. You can pass [ColorSheet.NO_COLOR]
     * to select noColorOption in the sheet.
     * @param noColorOption: Gives a option to set the [selectedColor] to [NO_COLOR]
     * @param listener: [ColorPickerListener]
     */
    fun colorPicker(
        colors: IntArray,
        @ColorInt selectedColor: Int? = null,
        noColorOption: Boolean = false,
        listener: ColorPickerListener
    ): ColorSheet {
        colorAdapter = ColorAdapter(this, colors, selectedColor, noColorOption, listener)
        return this
    }

    /**
     * Shows color sheet
     */
    fun show(fragmentManager: FragmentManager) {
        this.show(fragmentManager, TAG)
    }
}
