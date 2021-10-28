package com.kpstv.xclipper.extensions

import android.animation.Animator
import com.ferfalk.simplesearchview.SimpleSearchView

object DelegatedAnimator : Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator?) {}
    override fun onAnimationEnd(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationRepeat(animation: Animator?) {}
}

object DelegatedSearchViewListener : SimpleSearchView.SearchViewListener {
    override fun onSearchViewShown() {}
    override fun onSearchViewClosed() {}
    override fun onSearchViewShownAnimation() {}
    override fun onSearchViewClosedAnimation() {}
}