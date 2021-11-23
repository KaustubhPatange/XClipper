package com.kpstv.xclipper.ui.helpers.specials

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_EDIT
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.cwt.CWT
import com.kpstv.linkpreview.LinkPreview
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.dao.PreviewDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.Preview
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.databinding.BottomSheetMoreBinding
import com.kpstv.xclipper.di.SpecialEntryPoints
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.utils.PackageUtils
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.fragments.sheets.MoreChooserSheet
import com.kpstv.xclipper.ui.fragments.sheets.ShortenUriSheet
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.utils.LaunchUtils
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SetTextI18n")
class SpecialHelper(
    private val context: Context,
    private val supportFragmentManager: FragmentManager,
    private val lifecycleScope: CoroutineScope,
    private val clip: Clip,
) {
    private val dictionaryApiHelper: DictionaryApiHelper = SpecialEntryPoints.get(context).dictionaryApiHelper()
    private val linkPreviewDao: PreviewDao = SpecialEntryPoints.get(context).linkPreviewDao()

    private val enabledActions: List<SpecialAction> = SpecialSettings(context).getAllSetting()
    private val specialSettings = SpecialSettings(context)

    private val TAG = javaClass.simpleName
    private lateinit var adapter: MenuAdapter
    private val specialList = ArrayList<SpecialMenu>()

    private lateinit var onItemClick: SimpleFunction

    private val data = clip.data

    fun setActions(binding: BottomSheetMoreBinding, onItemClick: SimpleFunction) = with(binding) {
        this@SpecialHelper.onItemClick = onItemClick

        setDefineTag(this)

        setLinkPreview(this)

        setCommonOptions()

        setForEmail()

        setForMap()

        setForUrl()

        setPhoneNumber()

        setDateSpecials()

        setRecyclerView(this)
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

    private fun getAllTagValues(tagName: String) : List<String> {
        return clip.tags?.filter { it.key == tagName }?.map { it.value }?.distinct() ?: emptyList()
    }

    /** A common set of options that would appear in these section */
    private fun setCommonOptions() {
        if (enabledActions.contains(SpecialAction.SEARCH_QUERY)) {
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
    }

    /** A set of options when map tag is available */
    private fun setForMap() {
        /** Show search in maps menu */

        val checkForTag = clip.tags?.containsKey(ClipTag.MAP.small())
        val shouldBeAdded = clip.tags?.containsKey(ClipTag.URL.small()) == false && clip.tags?.containsKey(
            ClipTag.DATE.small()) == false

        val showMapMenu = SpecialMenu(
            image = R.drawable.ic_map,
            title = context.getString(R.string.search_map)
        ) {
            if (checkForTag == true) {
                createChooser(ClipTag.MAP.small()) { data ->
                    /** Search for coordinates */
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("geo:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            } else {
                /* Search as text */
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setData(Uri.parse("geo:0,0?q=${clip.data.replace("\\s+".toRegex(), "+")}"))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                runAction(intent)
            }
        }

        if (shouldBeAdded && enabledActions.contains(SpecialAction.SEARCH_MAP)) {
            specialList.add(showMapMenu)
        }
    }

    /** This will set options related to date tag */
    private fun setDateSpecials() {
        if (clip.tags?.containsKey(ClipTag.DATE.small()) == false) return

        val createCalenderMenu =
            SpecialMenu(title = context.getString(R.string.set_calender_event), image = R.drawable.ic_calender) {
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

        if (enabledActions.contains(SpecialAction.SET_CALENDER_EVENT)) {
            specialList.add(createCalenderMenu)
        }
    }

    /** This will set options if Email tag exist */
    private fun setForEmail() {
        val checkForTag = clip.tags?.containsKey(ClipTag.EMAIL.small())

        if (checkForTag == true) {
            /** Send an email */
            val sendEmail = SpecialMenu(
                image = R.drawable.ic_mail,
                title = context.getString(R.string.send_mail)
            ) {
                createChooser(ClipTag.EMAIL.small()) { data ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("mailto:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            }

            if (enabledActions.contains(SpecialAction.SEND_MAIL)) {
                specialList.add(sendEmail)
            }
        }
    }

    /** This will set function for phone number */
    private fun setPhoneNumber() {

        val phoneData = clip.tags?.containsKey(ClipTag.PHONE.small())
        val emailData = clip.tags?.containsKey(ClipTag.EMAIL.small())

        if (phoneData == true) {

            /** Make a phone call */
            val makeACallMenu = SpecialMenu(
                image = R.drawable.ic_call,
                title = context.getString(R.string.phone_call)
            ) {
                createChooser(ClipTag.PHONE.small()) { data ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("tel:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            }

            /** Send an sms */
            val sendSMSMenu = SpecialMenu(
                image = R.drawable.ic_message,
                title = context.getString(R.string.message_num)
            ) {
                createChooser(ClipTag.PHONE.small()) { data ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("smsto:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            }

            if (enabledActions.contains(SpecialAction.PHONE_CALL)) specialList.add(makeACallMenu)
            if (enabledActions.contains(SpecialAction.TEXT_NUMBER)) specialList.add(sendSMSMenu)

            /** Send a whatsapp message */
            if (PackageUtils.isPackageInstalled(context, "com.whatsapp")) {
                val whatsAppTextMenu =
                    SpecialMenu(image = R.drawable.ic_whatsapp, title = context.getString(R.string.send_whatsapp)) {
                        createChooser(ClipTag.PHONE.small()) { data ->
                            val numberToWhatsApp = when (data.length) {
                                10 -> "+${getCountryDialCode(context)} $data"
                                else -> data
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setData(Uri.parse("https://wa.me/$numberToWhatsApp"))
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            runAction(intent)
                        }
                    }

                if (enabledActions.contains(SpecialAction.TEXT_WHATSAPP)) specialList.add(whatsAppTextMenu)
            }
        }

        if (phoneData == true || emailData == true) {
            /** Add to contacts */
            val addToContactsMenu = SpecialMenu(
                image = R.drawable.ic_person_add,
                title = context.getString(R.string.canc)
            ) {
                val phoneNumbers = getAllTagValues(ClipTag.PHONE.small())
                val emailAddresses = getAllTagValues(ClipTag.EMAIL.small())

                if (phoneNumbers.isNotEmpty() && (phoneNumbers.size == emailAddresses.size)) {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE

                        val phoneContracts = listOf(ContactsContract.Intents.Insert.PHONE, ContactsContract.Intents.Insert.SECONDARY_PHONE, ContactsContract.Intents.Insert.TERTIARY_PHONE)
                        val emailContracts = listOf(ContactsContract.Intents.Insert.EMAIL, ContactsContract.Intents.Insert.SECONDARY_EMAIL, ContactsContract.Intents.Insert.TERTIARY_EMAIL)

                        for (i in 0..minOf(phoneNumbers.lastIndex, 2)) {
                            putExtra(phoneContracts[0], phoneNumbers[0])
                        }
                        for (i in 0..minOf(emailAddresses.lastIndex, 2)) {
                            putExtra(emailContracts[0], emailAddresses[0])
                        }
                    }
                    runAction(intent)
                }

                if (phoneData == true) {
                    createChooser(ClipTag.PHONE.small()) { data ->
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            type = ContactsContract.Contacts.CONTENT_TYPE

                            putExtra(ContactsContract.Intents.Insert.PHONE, data)
                        }

                        runAction(intent)
                    }
                }

                if (emailData == true) {
                    createChooser(ClipTag.EMAIL.small()) { data ->
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            type = ContactsContract.Contacts.CONTENT_TYPE

                            putExtra(ContactsContract.Intents.Insert.EMAIL, data)
                        }

                        runAction(intent)
                    }
                }
            }
            if (enabledActions.contains(SpecialAction.CREATE_CONTACT)) specialList.add(addToContactsMenu)
        }
    }


    /** This will set one of the item as shorten url*/
    private fun setForUrl() {

        val urlData = clip.tags?.containsKey(ClipTag.URL.small())

        if (urlData == true) {

            /** Add method for "Open link" */
            val openLinkMenu = SpecialMenu(
                image = R.drawable.ic_link,
                title = context.getString(R.string.open_link)
            ) {
                createChooser(ClipTag.URL.small()) { data ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse(data))
                    }

                    runAction(intent)
                }
            }

            /** Add method for "Open link privately" */
            val openLinkPrivateMenu = SpecialMenu(
                image = R.drawable.ic_incognito,
                title = context.getString(R.string.private_browse)
            ) {
                createChooser(ClipTag.URL.small()) { data ->
                    CWT.Builder(context)
                        .apply {
                            options.privateMode = true
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        .launch(data)

                    /** Dismiss the dialog */
                    onItemClick.invoke()
                }
            }

            /** Add method for "Shorten link" */
            val shortenUrl =
                SpecialMenu(R.drawable.ic_cut, context.getString(R.string.shorten_link)) {
                    createChooser(ClipTag.URL.small()) { data ->
                        val sheet = ShortenUriSheet(onItemClick)
                        sheet.arguments =
                            Bundle().apply { putString(ShortenUriSheet.LONG_URL, data) }
                        sheet.show(supportFragmentManager, "")
                    }
                }

            if (enabledActions.contains(SpecialAction.OPEN_LINK)) specialList.add(openLinkMenu)
            if (enabledActions.contains(SpecialAction.OPEN_PRIVATE)) specialList.add(openLinkPrivateMenu)
            if (enabledActions.contains(SpecialAction.SHORTEN_LINK)) specialList.add(shortenUrl)
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
    private fun setDefineTag(binding: BottomSheetMoreBinding) = with(binding) {
        SINGLE_WORD_PATTERN_REGEX.toRegex().let {
            if (it.containsMatchIn(data))
                dictionaryApiHelper.define(
                    word = it.find(data)?.value!!,
                    langCode = specialSettings.getDictionaryLang(),
                    responseListener = ResponseListener(
                        complete = { definition ->
                            editDefineWord.text = "$data:"
                            editDefine.text = HtmlCompat.fromHtml(
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

    private fun setRecyclerView(binding: BottomSheetMoreBinding) = with(binding) {
        bsmRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MenuAdapter(specialList, R.layout.item_special)
        bsmRecyclerView.adapter = adapter
        bsmRecyclerView.setHasFixedSize(true)
    }

    private fun setLinkPreview(binding: BottomSheetMoreBinding) = with(binding) {
        lifecycleScope.launch {
            val urlData = clip.tags?.containsKey(ClipTag.URL.small())
            if (urlData == true) {
                val topUrl = clip.tags?.firstValue(ClipTag.URL.small()) ?: return@launch
                val model = linkPreviewDao.getFromUrl(topUrl)
                if (model != null) {
                    linkPreview.setTitle(model.title)
                    linkPreview.setHostUrl(topUrl)
                    val subtitle = model.subtitle
                    if (subtitle != null)
                        linkPreview.setSubtitle(subtitle)
                    else {
                        linkPreview.setSubtitle(model.title)
                    }
                    if (model.imageUrl != null) linkPreview.setImage(model.imageUrl)
                } else {
                    linkPreview.loadPreview(topUrl, lifecycleScope)
                    linkPreview.loadCompleteListener =
                        LinkPreview.LinkPreviewListener { title, subtitle, imageUrl ->
                            lifecycleScope.launch {
                                val previewModel = Preview(
                                    title = title,
                                    subtitle = subtitle,
                                    imageUrl = imageUrl,
                                    url = topUrl
                                )
                                linkPreviewDao.insert(previewModel)
                            }
                        }
                }
                linkPreview.onClick {
                    LaunchUtils.commonUrlLaunch(context, topUrl)
                }
            }
        }
    }

    private fun getCountryDialCode(context: Context): String? {
        var countryDialCode: String? = null
        val telephonyMngr =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryId = telephonyMngr.simCountryIso.uppercase(Locale.ROOT)
        val arrCountryCode: Array<String> =
            context.resources.getStringArray(R.array.DialingCountryCode)
        for (i in arrCountryCode.indices) {
            val arrDial =
                arrCountryCode[i].split(",").toTypedArray()
            if (arrDial[1].trim { it <= ' ' } == countryId.trim()) {
                countryDialCode = arrDial[0]
                break
            }
        }
        return countryDialCode
    }

    private companion object {
        private const val SINGLE_WORD_PATTERN_REGEX = "^[^https?][^\\s\\W]+\$"
    }
}