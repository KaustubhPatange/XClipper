package com.kpstv.xclipper.ui.dialogs

import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import androidx.core.view.marginTop
import androidx.fragment.app.DialogFragment
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.utils.SystemUtils.isSystemOverlayEnabled
import com.kpstv.xclipper.feature_suggestions.R
import com.kpstv.xclipper.feature_suggestions.databinding.DialogBubbleConfigBinding
import com.kpstv.xclipper.ui.CoreDialogs
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject

@AndroidEntryPoint
class SuggestionDialogFragment : DialogFragment(R.layout.dialog_bubble_config) {

    private val binding by viewBinding(DialogBubbleConfigBinding::bind)

    @Inject lateinit var appSettings: AppSettings

    /**
     * Since overlay permission makes you to leave the activity, the only way
     * to check the preference is to set a boolean and then in onResume() we
     * will set the preference.
     */
    private var rememberToCheckOverlaySwitch = false

    private val statusBarInset: Int by lazy { requireActivity().statusBarHeight }
    private val bubbleCoordinates = Point()
    private var screenWidth = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchEnable.setOnClickListener {
            if (!isSystemOverlayEnabled(requireContext()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CoreDialogs.showSystemOverlayDialog(
                    context = requireContext(),
                    onPositiveClick = { rememberToCheckOverlaySwitch = true }
                )
                binding.switchEnable.isChecked = false
            }
        }
        binding.switchEnable.setOnCheckedChangeListener { _, _ -> updateConfigLayout() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnDemo.setOnClickListener { showDemoDialog() }
        binding.btnSave.setOnClickListener { saveOptions() }

        updateBubbleScreen()
        updateConfigLayout()
        updateXCoordinateTextView(0)
        updateYCoordinateTextView(0)
        setBubbleTouchTracking()
        binding.switchEnable.isChecked = appSettings.canShowClipboardSuggestions()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onResume() {
        if (rememberToCheckOverlaySwitch) {
            updateSwitchSetting()
            rememberToCheckOverlaySwitch = false
        }
        super.onResume()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBubbleTouchTracking() {
        binding.lvBubblecontainer.setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (event.x > screenWidth / 2) {
                        updateXCoordinateTextView(screenWidth)
                        binding.ivBubble.x = (screenWidth - binding.ivBubble.width).toFloat()
                    } else {
                        updateXCoordinateTextView(0)
                        binding.ivBubble.x = 0f
                    }
                    val clampedHeight = event.y.toInt().coerceIn(0, binding.lvBubblecontainer.height - binding.ivBubble.height)
                    updateYCoordinateTextView(clampedHeight)
                    binding.ivBubble.y = clampedHeight.toFloat()
                }
            }
            println("x: ${event.x}, y: ${event.y}, w: ${binding.lvBubblecontainer.width}, h: ${binding.lvBubblecontainer.height}")
            return@setOnTouchListener true
        }
    }

    private fun updateSwitchSetting() {
        binding.switchEnable.isChecked = isSystemOverlayEnabled(requireContext())
    }

    private fun updateBubbleScreen() {
        val bitmap = requireActivity().window.decorView.findViewById<View>(Window.ID_ANDROID_CONTENT).drawToBitmap()
        binding.ivScreen.setImageBitmap(bitmap)
        binding.ivScreen.doOnLayout {
            screenWidth = binding.ivScreen.width
            updateCoordinates()
        }
    }

    private fun updateCoordinates() {
        val coordinates = appSettings.getSuggestionBubbleCoordinates()
        if ((coordinates.first and Gravity.END) == Gravity.END) {
            binding.ivBubble.x = screenWidth.toFloat()
            updateXCoordinateTextView(screenWidth)
        }
        val yOffsetRelative = ((coordinates.second + statusBarInset) * binding.ivScreen.width) / resources.displayMetrics.widthPixels
        binding.ivBubble.y = yOffsetRelative

        updateYCoordinateTextView(yOffsetRelative.toInt())
    }

    private fun updateXCoordinateTextView(x: Int) {
        bubbleCoordinates.x = x
        binding.tvCoordinateX.text = getString(R.string.bubble_coordinate_x, x.toString())
    }

    private fun updateYCoordinateTextView(y: Int) {
        bubbleCoordinates.y = y
        binding.tvCoordinateY.text = getString(R.string.bubble_coordinate_y, y.toString())
    }

    private fun updateConfigLayout() {
        val isChecked = binding.switchEnable.isChecked
        if (isChecked) {
            TransitionManager.beginDelayedTransition(binding.root, Fade())
            binding.lvConfig.show()
        } else {
            binding.lvConfig.collapse()
        }
    }

    private fun showDemoDialog() {

    }

    private fun saveOptions() {
        val gravity = if (bubbleCoordinates.x > screenWidth / 2) {
            Gravity.TOP or Gravity.END
        } else {
            Gravity.TOP or Gravity.START
        }
        val yOffset = (resources.displayMetrics.widthPixels * (bubbleCoordinates.y)) / screenWidth
        appSettings.setSuggestionBubbleCoordinates(gravity, yOffset.toFloat() - statusBarInset)
        appSettings.setShowClipboardSuggestions(binding.switchEnable.isChecked)
        Toasty.info(requireContext(), getString(R.string.bubble_settings_saved)).show()
        dismiss()
    }
}