package com.kpstv.xclipper.ui.fragments

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.transition.TransitionInflater
import com.kpstv.navigation.ValueFragment
import com.kpstv.xclipper.R

open class AnimateFragment(@LayoutRes resId: Int) : ValueFragment(resId) {
    open val toAnimate: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (toAnimate){
            val inflater = TransitionInflater.from(requireContext())
            enterTransition = inflater.inflateTransition(R.transition.fade)
            exitTransition = inflater.inflateTransition(R.transition.fade)
        }
    }
}

@Deprecated("Navigator supports transitions")
open class AnimatePreferenceFragment : PreferenceFragmentCompat() {
    open val toAnimate: Boolean = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (toAnimate){
            val inflater = TransitionInflater.from(requireContext())
            enterTransition = inflater.inflateTransition(R.transition.fade)
            exitTransition = inflater.inflateTransition(R.transition.fade)
        }
    }
}