package com.kpstv.xclipper.ui.helpers.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.KClass

abstract class AbstractFragmentHelper<T: Fragment>(
    private val activity: FragmentActivity,
    clazz: KClass<T>
) {
    protected abstract fun onFragmentViewCreated()
    open fun onFragmentResumed() { }
    open fun onFragmentDestroyed() { }

    /**
     * This will register an instance of this class to fragment manager
     * and will automatically unregister the callback when the activity
     * is destroyed.
     */
    open fun register() {
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentLifeCycleCallbacks, true
        )

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                activity.supportFragmentManager
                    .unregisterFragmentLifecycleCallbacks(fragmentLifeCycleCallbacks)
                super.onDestroy(owner)
            }
        })
    }

    private inline fun <reified T : Fragment> T.matches(clazz: KClass<*>): Boolean {
        return this.javaClass == clazz.java
    }

    private val fragmentLifeCycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                if (f.matches(clazz))
                    onFragmentViewCreated()
            }

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                if (f.matches(clazz))
                    onFragmentResumed()
            }

            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                if (f.matches(clazz))
                    onFragmentDestroyed()
                super.onFragmentViewDestroyed(fm, f)
            }
        }
}