package com.kpstv.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kpstv.navigation.Destination
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.onboarding.internals.OnBoardingNavViewModel
import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.welcome.R
import com.kpstv.welcome.databinding.FragmentOnboardingBinding
import com.kpstv.xclipper.extensions.viewBinding

class OnBoardingFragment : ValueFragment(R.layout.fragment_onboarding), FragmentNavigator.Transmitter {
    private val binding by viewBinding(FragmentOnboardingBinding::bind)
    private val navViewModel by viewModels<OnBoardingNavViewModel>()

    private lateinit var navigator: FragmentNavigator

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(binding.root, Destination.of(OnBoardingRoutes.GREET.clazz))

        navViewModel.navigation.observe(viewLifecycleOwner, navigationObserver)
    }

    private val navigationObserver = Observer { options: OnBoardingNavViewModel.NavigationOptions? ->
        options?.let { navigator.navigateTo(it.clazz, it.navOptions) }
    }
}