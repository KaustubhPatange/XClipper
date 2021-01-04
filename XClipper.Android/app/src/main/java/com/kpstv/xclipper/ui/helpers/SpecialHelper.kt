package com.kpstv.xclipper.ui.helpers

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_EDIT
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.cwt.CWT
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.extensions.containsKey
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.small
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.fragments.sheets.MoreChooserSheet
import com.kpstv.xclipper.ui.fragments.sheets.ShortenUriSheet
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.bottom_sheet_more.view.*
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SetTextI18n")
class SpecialHelper(
    private val context: Context,
    private val tinyUrlApiHelper: TinyUrlApiHelper,
    private val dictionaryApiHelper: DictionaryApiHelper,
    private val supportFragmentManager: FragmentManager,
    private val clipboardProvider: ClipboardProvider,
    private val clip: Clip,
    @Deprecated("Dialog is deprecated")
    private val isDialog: Boolean = false
) {
    private val TAG = javaClass.simpleName
    private lateinit var adapter: MenuAdapter
    private val specialList = ArrayList<SpecialMenu>()

    private lateinit var onItemClick: SimpleFunction

    private val data = clip.data
    fun setActions(view: View, onItemClick: SimpleFunction) = with(view) {
        this@SpecialHelper.onItemClick = onItemClick

        if (isDialog) {
            setForDialog(this)
        }

        setDefineTag(this)

        setSearchButton()

        setForEmail()

        setForMap()

        setForUrl()

        setPhoneNumber()

        setDateSpecials()

        setRecyclerView(this)
    }

    @Deprecated("Dialog is deprecated")
    private fun setForDialog(view: View): Unit = with(view) {
        bsm_notch.hide()

        val currentClip =
            clipboardProvider.getClipboard()?.getItemAt(0)?.coerceToText(context)?.toString()
        if (currentClip != data) {
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_copy_white,
                    title = context.getString(R.string.set_current_clip)
                ) {
                    clipboardProvider.ignoreChange {
                        clipboardProvider.setClipboard(ClipData.newPlainText(data, data))
                    }

                    /** Dismiss the dialog */
                    onItemClick.invoke()
                }
            )
        }
    }

    private fun createChooser(tagName: String, onItemSelected: (String) -> Unit) {
        val items = clip.tags?.filter { it.key == tagName }?.map { it.value }?.distinct() ?: return
        if (items.size == 1) {
            onItemSelected.invoke(items[0])
            return
        }
        val sheet = MoreChooserSheet(items, onItemSelected)
        sheet.show(supportFragmentManager, "")
    }

    /** A common set of options that would appear in these section */
    private fun setSearchButton() {
        specialList.add(
            SpecialMenu(
                image = R.drawable.ic_search,
                title = context.getString(R.string.search_web)
            ) {
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, data)

                runAction(intent)
            }
        )
    }

    /** A set of options when map tag is available */
    private fun setForMap() {
        val checkForTag = clip.tags?.containsKey(ClipTag.MAP.small())

        if (checkForTag == true) {

            /** Show search in maps menu */
            val showMapMenu = SpecialMenu(image = R.drawable.ic_map, title = context.getString(R.string.search_map)) {
                createChooser(ClipTag.MAP.small()) { data ->
                    /** Search for coordinates */
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("geo:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            }

            specialList.add(showMapMenu)
        }
    }

        /** This will set options related to date tag */
        private fun setDateSpecials() {
            if (clip.tags?.containsKey(ClipTag.DATE.small()) == false) return

            val createCalenderMenu = SpecialMenu(title = "Set a calender event", image = R.drawable.ic_calender) {
                createChooser(ClipTag.DATE.small()) { data ->
                    /** Parse the date now */
                    val dateValues = data.split("/", ".", "-", " ")

                    val year = if (dateValues[0].length == 4) dateValues[0] else dateValues[2]
                    val month = dateValues[1]
                    val day =
                        if (dateValues[0].length in 2 downTo 1) dateValues[0] else dateValues[2]

                    val eventTime = Calendar.getInstance()
                    eventTime.set(year.toInt(), month.toInt(), day.toInt())
                    val intent = Intent(ACTION_EDIT).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        type = "vnd.android.cursor.item/event"
                        putExtra(
                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            eventTime.timeInMillis
                        )
                        putExtra(
                            CalendarContract.EXTRA_EVENT_END_TIME,
                            eventTime.timeInMillis
                        )
                    }
                    runAction(intent)
                }
            }

            specialList.add(createCalenderMenu)
        }

        /** This will set options if Email tag exist */
        private fun setForEmail() {
            val checkForTag = clip.tags?.containsKey(ClipTag.EMAIL.small())

            if (checkForTag == true) {
                /** Send an email */
                val sendEmail =  SpecialMenu(image = R.drawable.ic_mail, title = context.getString(R.string.send_mail)) {
                    createChooser(ClipTag.EMAIL.small()) { data ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setData(Uri.parse("mailto:$data"))
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        runAction(intent)
                    }
                }

                specialList.add(sendEmail)
            }
        }

        /** This will set function for phone number */
        private fun setPhoneNumber() {

            val urlData = clip.tags?.containsKey(ClipTag.PHONE.small())

            if (urlData == true) {

                /** Make a phone call */
                val makeACallMenu =  SpecialMenu(image = R.drawable.ic_call, title = context.getString(R.string.phone_call)) {
                    createChooser(ClipTag.PHONE.small()) { data ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setData(Uri.parse("tel:$data"))
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        runAction(intent)
                    }
                }

                /** Add to contacts */
                val addToContactsMenu = SpecialMenu(image = R.drawable.ic_person_add, title = context.getString(R.string.canc)) {
                    createChooser(ClipTag.PHONE.small()) { data ->
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            type = ContactsContract.Contacts.CONTENT_TYPE

                            putExtra(ContactsContract.Intents.Insert.PHONE, data)
                        }

                        runAction(intent)
                    }
                }

                /** Send an sms */
                val sendSMSMenu = SpecialMenu(image = R.drawable.ic_message, title = context.getString(R.string.message_num)) {
                    createChooser(ClipTag.PHONE.small()) { data ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setData(Uri.parse("smsto:$data"))
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        runAction(intent)
                    }
                }

                specialList.add(makeACallMenu)
                specialList.add(addToContactsMenu)
                specialList.add(sendSMSMenu)

                /** Send a whatsapp message */
                if (Utils.isPackageInstalled(context, "com.whatsapp")) {
                    val whatsAppTextMenu = SpecialMenu(image = R.drawable.ic_whatsapp, title = "WhatsApp this number") {
                        createChooser(ClipTag.PHONE.small()) { data ->
                            val numberToWhatsApp = when (data.length) {
                                10 -> "+${Utils.getCountryDialCode(context)} $data"
                                else -> data
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setData(Uri.parse("https://wa.me/$numberToWhatsApp"))
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            runAction(intent)
                        }
                    }

                    specialList.add(whatsAppTextMenu)
                }
            }
        }


        /** This will set one of the item as shorten url*/
        private fun setForUrl() {

            val urlData = clip.tags?.containsKey(ClipTag.URL.small())

            if (urlData == true) {

                /** Add method for "Open link" */
                val openLinkMenu = SpecialMenu(image = R.drawable.ic_link, title = context.getString(R.string.open_link)) {
                    createChooser(ClipTag.URL.small()) { data ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setData(Uri.parse(data))
                        }

                        runAction(intent)
                    }
                }

                /** Add method for "Open link privately" */
                val openLinkPrivateMenu =  SpecialMenu(image = R.drawable.ic_incognito, title = context.getString(R.string.private_browse)) {
                    createChooser(ClipTag.URL.small()) { data ->
                        CWT.Builder(context)
                            .apply { options.privateMode = true }
                            .launch(data)

                        /** Dismiss the dialog */
                        onItemClick.invoke()
                    }
                }

                /** Add method for "Shorten link" */
                val shortenUrl = SpecialMenu(R.drawable.ic_cut, context.getString(R.string.shorten_link)) {
                    createChooser(ClipTag.URL.small()) { data ->
                        val sheet = ShortenUriSheet(onItemClick)
                        sheet.arguments =
                            Bundle().apply { putString(ShortenUriSheet.LONG_URL, data) }
                        sheet.show(supportFragmentManager, "")
                    }
                }

                specialList.add(openLinkMenu)
                specialList.add(openLinkPrivateMenu)
                specialList.add(shortenUrl)
            }
        }

        /** This will perform startActivity on intent  */
        private fun runAction(intent: Intent) {
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toasty.error(context, context.getString(R.string.err_action)).show()
            }

            /** Dismiss the dialog */
            onItemClick.invoke()
        }

        /**
         * This will set the define text below "Specials" text. It will perform some checks
         * before setting the define.
         */
        private fun setDefineTag(view: View) = with(view) {
            App.SINGLE_WORD_PATTERN_REGEX.toRegex().let {
                if (it.containsMatchIn(data))
                    dictionaryApiHelper.define(
                        it.find(data)?.value!!, ResponseListener(
                            complete = { definition ->
                                edit_define_word.text = "$data:"
                                edit_define.text = HtmlCompat.fromHtml(
                                    """
                                <i>${definition.define} </i>
                            """.trimIndent().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY
                                ) //<a href="https://google.com">more</a>
                                defineLayout.show()
                            },
                            error = { e -> Log.e(TAG, "Error: ${e.message}") }
                        )
                    )
            }
        }


        private fun setRecyclerView(view: View) = with(view) {
            bsm_recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = MenuAdapter(specialList, R.layout.item_special)
            bsm_recyclerView.adapter = adapter
            bsm_recyclerView.setHasFixedSize(true)
        }
    }