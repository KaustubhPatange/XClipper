package com.kpstv.xclipper.extensions

import android.animation.Animator

object DelegatedAnimator : Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator?) {}
    override fun onAnimationEnd(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationRepeat(animation: Animator?) {}
}