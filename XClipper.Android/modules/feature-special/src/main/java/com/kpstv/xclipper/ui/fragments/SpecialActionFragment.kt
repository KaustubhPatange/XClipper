package com.kpstv.xclipper.ui.fragments

import android.os.Bundle
import com.kpstv.xclipper.ui.dialogs.SingleSelectDialogBuilder
import com.kpstv.xclipper.ui.dialogs.SingleSelectModel2
import com.kpstv.xclipper.extensions.SpecialAction
import com.kpstv.xclipper.feature_special.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpecialActionFragment : ActionFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Search on Map */
        setCheckedItem(
            title = getString(R.string.search_query_action),
            message = getString(R.string.search_query_action_text),
            icon = R.drawable.sp_ic_search,
            action = SpecialAction.SEARCH_QUERY
        )

        /* Search on Map */
        setCheckedItem(
            title = getString(R.string.search_map_action),
            message = getString(R.string.search_map_action_text),
            icon = R.drawable.sp_ic_map,
            action = SpecialAction.SEARCH_MAP
        )

        /* Open link */
        setCheckedItem(
            title = getString(R.string.open_link_action),
            message = getString(R.string.open_link_action_text),
            icon = R.drawable.ic_link,
            action = SpecialAction.OPEN_LINK
        )

        /* Shorten link */
        setCheckedItem(
            title = getString(R.string.shorten_link_action),
            message = getString(R.string.shorten_link_action_text),
            icon = R.drawable.sp_ic_cut,
            action = SpecialAction.SHORTEN_LINK
        )

        /* Open link in private */
        setCheckedItem(
            title = getString(R.string.open_link_private_action),
            message = getString(R.string.open_link_private_action_text),
            icon = R.drawable.sp_ic_incognito,
            action = SpecialAction.OPEN_PRIVATE
        )

        /* Create contact action */
        setCheckedItem(
            title = getString(R.string.create_contact_action),
            message = getString(R.string.create_contact_action_text),
            icon = R.drawable.sp_contact_add,
            action = SpecialAction.CREATE_CONTACT
        )

        /* Create calender event */
        setCheckedItem(
            title = getString(R.string.calender_events_action),
            message = getString(R.string.calender_events_action_text),
            icon = R.drawable.sp_ic_calender,
            action = SpecialAction.SET_CALENDER_EVENT
        )

        /* Call Number action */
        setCheckedItem(
            title = getString(R.string.call_number_action),
            message = getString(R.string.call_number_action_text),
            icon = R.drawable.sp_ic_call,
            action = SpecialAction.PHONE_CALL
        )

        /* Text Number action */
        setCheckedItem(
            title = getString(R.string.send_text_message_action),
            message = getString(R.string.send_text_message_action_text),
            icon = R.drawable.sp_ic_message,
            action = SpecialAction.TEXT_NUMBER
        )

        /* Whatsapp action */
        setCheckedItem(
            title = getString(R.string.send_whatsapp_action),
            message = getString(R.string.send_whatsapp_action_text),
            icon = R.drawable.sp_ic_whatsapp,
            action = SpecialAction.TEXT_WHATSAPP
        )

        /* Send an email action */
        setCheckedItem(
            title = getString(R.string.send_email_action),
            message = getString(R.string.send_email_action_text),
            icon = R.drawable.sp_ic_send_mail,
            action = SpecialAction.SEND_MAIL
        )

        /* Set Lang code for define */ // TODO: Later move into separate setting fragment where you can enable & disable it
        setCommonItem(
            title = getString(R.string.lang_title),
            message = getString(R.string.lang_summary),
            icon = -1
        )
    }

    override fun getItemClickListener(item: ActionItem, position: Int) {
        if (item.title == getString(R.string.lang_title)) {
            val currentLang = specialSettings.getDictionaryLang()
            val entries = resources.getStringArray(R.array.lang_entries).filterNotNull().toList()
            val values = resources.getStringArray(R.array.lang_values).filterNotNull().toList()
            val items = entries.zip(values).map { SingleSelectModel2(it.first, it.second) }
            SingleSelectDialogBuilder(
                context = requireContext(),
                onSelect = { specialSettings.setDictionaryLang(values[it]) }
            ).run {
                setTitle(getString(R.string.lang_dialog_title))
                setItems(items)
                highLightItemPosition(values.indexOf(currentLang))
                show()
            }
        }
        // TODO: Implement this
    }
}