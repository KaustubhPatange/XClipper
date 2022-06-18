package com.kpstv.xclipper.ui.fragments.settings.look_feel

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.kpstv.xclipper.AddOns
import com.kpstv.xclipper.di.SettingScreenHandler
import com.kpstv.xclipper.extensions.QuickTip
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.ui.viewmodel.SettingNavViewModel

class CustomizeQuickTipPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {
    init {
        layoutResource = R.layout.custom_quick_tip
    }

    private lateinit var settingScreenHandler: SettingScreenHandler
    private lateinit var settingNavViewModel: SettingNavViewModel

    fun inject(settingScreenHandler: SettingScreenHandler, settingNavViewModel: SettingNavViewModel) {
        this.settingScreenHandler = settingScreenHandler
        this.settingNavViewModel = settingNavViewModel
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) : Unit = with(context) {
        QuickTip(holder.itemView as FrameLayout).apply {
            setTitleText(R.string.ct_premium_title)
            setSubText(R.string.ct_premium_text)
            setIcon(R.drawable.addons_ic_crown_colored)
            hideButtonPanel()
            applyColor(colorFrom(R.color.golden_yellow))
            updatePadding(top = 7.dp())
            setOnClick {
                val upgradeDefinition = settingScreenHandler.screenUpgrade()
                val upgradeArgs = settingScreenHandler.argsScreenUpgrade {
                    setHighlightExtensionPosition(context, AddOns.getCustomizeThemeExtension(context))
                }
                settingNavViewModel.navigateTo(
                    screenDefinition = upgradeDefinition,
                    args = upgradeArgs
                )
            }
            create()
        }
    }
}