package com.kpstv.xclipper.extension

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.feature_home.R

val ClipTag.titleRes: Int @StringRes get() = when(this) {
    ClipTag.LOCK -> R.string.tag_lock
    ClipTag.PHONE -> R.string.tag_phone
    ClipTag.DATE -> R.string.tag_date
    ClipTag.URL -> R.string.tag_url
    ClipTag.EMAIL -> R.string.tag_email
    ClipTag.MAP -> R.string.tag_map
}

val ClipTag.drawableRes: Int @DrawableRes get() = when(this) {
    ClipTag.LOCK -> R.drawable.fh_ic_lock
    ClipTag.PHONE -> R.drawable.fh_ic_phone
    ClipTag.DATE -> R.drawable.fh_ic_date
    ClipTag.URL -> R.drawable.fh_ic_url
    ClipTag.EMAIL -> R.drawable.fh_ic_mail
    ClipTag.MAP -> R.drawable.fh_ic_map
}