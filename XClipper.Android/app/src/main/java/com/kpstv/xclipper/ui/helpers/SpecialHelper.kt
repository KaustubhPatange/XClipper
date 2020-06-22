package com.kpstv.xclipper.ui.helpers

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.small
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.dialogs.AllPurposeDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.bottom_sheet_more.view.*

@SuppressLint("SetTextI18n")
class SpecialHelper(
    private val context: Context,
    private val tinyUrlApiHelper: TinyUrlApiHelper,
    private val dictionaryApiHelper: DictionaryApiHelper,
    private val supportFragmentManager: FragmentManager,
    private val clip: Clip
) {
    private val TAG = javaClass.simpleName
    private lateinit var adapter: MenuAdapter
    private val specialList = ArrayList<SpecialMenu>()

    private lateinit var onItemClick: SimpleFunction

    private val data = clip.data
    fun setActions(view: View, onItemClick: SimpleFunction) = with(view){
        this@SpecialHelper.onItemClick = onItemClick

        setDefineTag(this)

        setSearchButton(this)

        setForEmail(this)

        setForMap(this)

        setShortenUrl(this)

        setPhoneNumber(this)

        setRecyclerView(this)
    }

    /** A common set of options that would appear in these section */
    private fun setSearchButton(view: View) = with(view) {
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
    private fun setForMap(view: View) = with(view) {
        val checkForTag = clip.tags?.containsKey(ClipTag.MAP.small())

        if (checkForTag == true) {
            val data = clip.tags?.getValue(ClipTag.MAP.small())

            /** Search for coordinates */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_map,
                    title = context.getString(R.string.search_map)
                ) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("geo:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            )
        }
    }


    /** This will set options if Email tag exist */
    private fun setForEmail(view: View) = with(view) {

        val checkForTag = clip.tags?.containsKey(ClipTag.EMAIL.small())

        if (checkForTag == true) {
            val data = clip.tags?.getValue(ClipTag.EMAIL.small())

            /** Send an email */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_mail,
                    title = context.getString(R.string.send_mail)
                ) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("mailto:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            )
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
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("tel:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            )

            /** Add to contacts */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_person_add,
                    title = context.getString(R.string.canc)
                ) {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE

                        putExtra(ContactsContract.Intents.Insert.PHONE, data)
                    }

                    runAction(intent)
                }
            )

            /** Send an sms */
            specialList.add(
                SpecialMenu(
                    image = R.drawable.ic_message,
                    title = context.getString(R.string.message_num)
                ) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse("smsto:$data"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    runAction(intent)
                }
            )


            val numberToWhatsApp = when {
                data?.length!! == 10 -> "+${Utils.getCountryDialCode(context)} $data"
                else -> data
            }

            /** Send a whatsapp message */
            if (Utils.isPackageInstalled(context, "com.whatsapp")) {
                specialList.add(
                    SpecialMenu(
                        image = R.drawable.ic_whatsapp,
                        title = "WhatsApp this number"
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setData(Uri.parse("https://wa.me/$numberToWhatsApp"))
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        runAction(intent)
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
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(Uri.parse(data))
                    }

                    runAction(intent)
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

                    /** Initiate creation of shorten url. */
                    tinyUrlApiHelper.createShortenUrl(data!!, ResponseListener(
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
                    onItemClick.invoke()
                }
            )
        }

    }

    /** This will perform startActivity on intent  */
    private fun runAction(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toasty.error(context, context.getString(R.string.err_action)).show()
        }

        /** Dismiss the dialog from this callback hell */
        onItemClick.invoke()
    }

    /**
     * This will set the define text below "Specials" text. It will perform some checks
     * before setting the define.
     */
    private fun setDefineTag(view: View) = with(view) {
        App.SINGLE_WORD_PATTERN_REGEX.toRegex().let {
            if (it.containsMatchIn(data!!))
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