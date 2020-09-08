package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.App.TUTORIAL_PREF
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class WatchVideo : Fragment(R.layout.fragment_welcome), KodeinAware {

    override val kodein by kodein()
    private val preferenceProvider by instance<PreferenceProvider>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette7,
            nextPaletteId = R.color.colorPrimary,
            textId = R.string.palette7_text,
            nextTextId = R.string.next_8,
            action = {
                preferenceProvider.putBooleanKey(TUTORIAL_PREF, true)

                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_greet, true)
                    .build()

                findNavController().navigate(
                    WatchVideoDirections.actionWatchVideoToFragmentHome(),
                    options
                )
            }/*,
            insertView = LayoutInflater.from(context).inflate(
                R.layout.layout_watch, null
            ).apply {
                btn_common.setOnClickListener {
                    val intent = Intent(ACTION_VIEW).apply {
                        flags = FLAG_ACTIVITY_NEW_TASK
                        data = Uri.parse("https://google.com")
                    }
                    requireContext().startActivity(intent)
                }
            }*/
        )
    }
}