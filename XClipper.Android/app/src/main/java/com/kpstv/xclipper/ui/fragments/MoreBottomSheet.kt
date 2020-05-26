package com.kpstv.xclipper.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.SINGLE_WORD_PATTERN_REGEX
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.small
import com.kpstv.xclipper.extensions.utils.Utils.Companion.getCountryDialCode
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isPackageInstalled
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.dialogs.AllPurposeDialog
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.bottom_sheet_more.view.*


class MoreBottomSheet(
    private val mainViewModel: MainViewModel,
    private val supportFragmentManager: FragmentManager,
    private val clip: Clip
) : RoundedBottomSheetDialogFragment() {
    private val TAG = javaClass.simpleName
    private lateinit var adapter: MenuAdapter
    private val specialList = ArrayList<SpecialMenu>()

    private val data = clip.data?.Decrypt()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        with(inflater.inflate(R.layout.bottom_sheet_more, container, false)) {

            setDefineTag(this)

            setShortenUrl(this)

            setPhoneNumber(this)

            setRecyclerView(this)

            return this
        }
    }

    /** This will set function for phone number */
    private fun setPhoneNumber(view: View) = with(view) {

        val urlData = clip.tags?.containsKey(ClipTag.PHONE.small())

        if (urlData == true) {
            val data = clip.tags?.getValue(ClipTag.PHONE.small())

            /** Make a phone call */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_call,
                    title = context.getString(R.string.phone_call)
                ) {
                    val intent = Intent(ACTION_VIEW).apply {
                        setData(Uri.parse("tel:$data"))
                        flags = FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        startActivity(intent)
                    }catch (e: Exception) {
                        Toasty.info(context, context.getString(R.string.err_action)).show()
                    }

                    /** Closing this bottom sheet */
                    dismiss()
                }
            )

            /** Add to contacts */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_person_add,
                    title = context.getString(R.string.canc)
                ) {
                    val intent = Intent(ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE

                        putExtra(ContactsContract.Intents.Insert.PHONE, data)
                    }

                    try {
                        startActivity(intent)
                    }catch (e: Exception) {
                        Toasty.info(context, context.getString(R.string.err_action)).show()
                    }

                    /** Closing this bottom sheet */
                    dismiss()
                }
            )

            /** Send an sms */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_message,
                    title = context.getString(R.string.message_num)
                ) {
                    val intent = Intent(ACTION_VIEW).apply {
                        setData(Uri.parse("smsto:$data"))
                        flags = FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        startActivity(intent)
                    }catch (e: Exception) {
                        Toasty.info(context, context.getString(R.string.err_action)).show()
                    }

                    /** Closing this bottom sheet */
                    dismiss()
                }
            )


            val numberToWhatsApp = when {
                data?.length!! == 10 -> "+${getCountryDialCode(context)} $data"
                else -> data
            }

            /** Send a whatsapp message */
            if (isPackageInstalled(context, "com.whatsapp")) {
                specialList.add(
                    SpecialMenu(
                        image = R.drawable.ic_whatsapp,
                        title = "WhatsApp this number"
                    ) {
                        val intent = Intent(ACTION_VIEW).apply {
                            setData(Uri.parse("https://wa.me/$numberToWhatsApp"))
                            flags = FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            startActivity(intent)
                        }catch (e: Exception) {
                            Toasty.info(context, context.getString(R.string.err_action)).show()
                        }

                        /** Closing this bottom sheet */
                        dismiss()
                    }
                )
            }
        }
    }


    /** This will set one of the item as shorten url*/
    private fun setShortenUrl(view: View) = with(view) {

        val urlData = clip.tags?.containsKey(ClipTag.URL.small())

        if (urlData == true) {
            val data = clip.tags?.getValue(ClipTag.URL.small())

            /** Add method for "Open link" */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_link,
                    title = context.getString(R.string.open_link)
                ) {
                    val intent = Intent(ACTION_VIEW).apply {
                        setData(Uri.parse(data))
                    }
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toasty.error(context, context.getString(R.string.err_action))
                    }

                    /** Closing this bottom sheet */
                    dismiss()
                }

            )

            /** Add method for "Shorten link" */
            specialList.add(
                SpecialMenu(
                    R.drawable.ic_cut,
                    context.getString(R.string.shorten_link)
                ) {
                    val dialog = AllPurposeDialog()
                        .setIsProgressDialog(true)
                    dialog.show(supportFragmentManager, "blank")

                    /** Initiate creation of shorten url. */
                    mainViewModel.tinyUrlApiHelper.createShortenUrl(data!!, ResponseListener(
                        complete = {

                            /** We've the shorten url. */
                            dialog.setMessage(it.shortUrl)
                                .setIsProgressDialog(false)
                                .setShowPositiveButton(true)
                                .setToolbarMenu(R.menu.url_menu)
                                .setToolbarMenuItemListener { item ->
                                    if (item.itemId == R.id.action_copy) {

                                        /** Set shorten url to clipboard */
                                        val clipboardManager =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboardManager.setPrimaryClip(
                                            ClipData.newPlainText(
                                                it.shortUrl,
                                                it.shortUrl
                                            )
                                        )

                                        Toasty.info(context, context.getString(R.string.ctc)).show()

                                        /** Close the dialog box */
                                        dialog.dismiss()
                                    }
                                    true
                                }
                                .update()
                        },
                        error = {
                            dialog.dismiss()
                            Toasty.error(context, "Error: ${it.message}").show()
                        }
                    ))

                    /** Dismiss the dialog from this callback hell */

                    dismiss()
                }
            )
        }

    }

    /**
     * This will set the define text below "Specials" text. It will perform some checks
     * before setting the define.
     */
    private fun setDefineTag(view: View) = with(view) {
        SINGLE_WORD_PATTERN_REGEX.toRegex().let {
            if (it.containsMatchIn(data!!))
                mainViewModel.dictionaryApiHelper.define(
                    it.find(data)?.value!!, ResponseListener(
                        complete = { definition ->
                            edit_define.text = HtmlCompat.fromHtml(
                                """
                                <i>${definition.define} <a href="https://google.com">more</a></i>
                            """.trimIndent().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            defineLayout.show()
                        },
                        error = { e -> Log.e(TAG, "Error: ${e.message}") }
                    )
                )
        }
    }


    private fun setRecyclerView(view: View) = with(view) {
        bsm_recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MenuAdapter(specialList)
        bsm_recyclerView.adapter = adapter
        bsm_recyclerView.setHasFixedSize(true)
    }
}