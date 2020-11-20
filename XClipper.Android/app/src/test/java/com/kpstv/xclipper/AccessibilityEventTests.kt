package com.kpstv.xclipper

import android.view.accessibility.AccessibilityEvent
import com.kpstv.xclipper.extensions.toLines
import com.kpstv.xclipper.service.helper.ClipboardDetection
import okhttp3.internal.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.system.measureTimeMillis

class AccessibilityEventTests {

    // TODO: Add more tests

    @Test
    fun test() {
        val r = "^\\[(.*)]\$".toRegex()
        val s = "[Copy link address,ssw]"
        println(r.find(s)?.groupValues?.get(1)?.split(","))

        val totalTime = measureTimeMillis {
            for (i in 1 until 10) {
                val string =
                    "EventType: TYPE_VIEW_LONG_CLICKED; EventTime: 50454360; PackageName: com.medium.reader; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Some reference which helped me to learn more about actions are as follows.]; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0"
                val event = EventsHelper.parse(string)
            }
        }

        println("Total time: $totalTime")


        println(0x00000800.toHexString().toLong(16))
        val sf = EventsHelper.getEventType("TYPE_WINDOW_CONTENT_CHANGED")
        //ClipboardDetection.addEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)
//        ClipboardDetection.addEvent()
    }

    @Test
    fun `Medium long click & Copy`() {
        val string = """
EventType: TYPE_VIEW_LONG_CLICKED; EventTime: 50454360; PackageName: com.medium.reader; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Some reference which helped me to learn more about actions are as follows.]; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_VIEW_FOCUSED; EventTime: 50454368; PackageName: com.medium.reader; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Some reference which helped me to learn more about actions are as follows.]; ContentDescription: null; ItemCount: 22; CurrentItemIndex: 7; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_VIEW_TEXT_SELECTION_CHANGED; EventTime: 50454371; PackageName: com.medium.reader; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Some reference which helped me to learn more about actions are as follows.]; ContentDescription: null; ItemCount: 74; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: 15; ToIndex: 20; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_STATE_CHANGED; EventTime: 50454906; PackageName: com.medium.reader; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.FrameLayout; Text: [More options, Highlight, Respond, Edit, Tweet]; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_VIEW_TEXT_SELECTION_CHANGED; EventTime: 50458480; PackageName: com.medium.reader; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Some reference which helped me to learn more about actions are as follows.]; ContentDescription: null; ItemCount: 74; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: 20; ToIndex: 20; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
        """.trimIndent()

        assertEquals(true, runEventTest(string))
    }

    @Test
    fun `Share chooser "Copy" button`() {
        val string = """
EventType: TYPE_WINDOW_STATE_CHANGED; EventTime: 262070255; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: com.android.internal.app.ChooserActivity; Text: [Share]; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: true; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 262070325; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.ScrollView; Text: []; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 262070530; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_TEXT]; WindowChangeTypes: [] [ ClassName: android.widget.Button; Text: []; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 1
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 262070760; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE, CONTENT_CHANGE_TYPE_TEXT]; WindowChangeTypes: [] [ ClassName: android.widget.ScrollView; Text: []; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 262070860; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.ListView; Text: []; ContentDescription: null; ItemCount: 18; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: true; BeforeText: null; FromIndex: 0; ToIndex: 7; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 262070961; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.ListView; Text: []; ContentDescription: null; ItemCount: 18; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: true; BeforeText: null; FromIndex: 0; ToIndex: 7; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 262071062; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.ListView; Text: []; ContentDescription: null; ItemCount: 18; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: true; BeforeText: null; FromIndex: 0; ToIndex: 7; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_VIEW_CLICKED; EventTime: 262072531; PackageName: android; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.LinearLayout; Text: [Copy]; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 1
        """.trimIndent()

        assertEquals(true, runEventTest(string))
    }

    @Test
    fun `Long press & select "Copy"`() {
        val string = """
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 258990954; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.FrameLayout; Text: []; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_STATE_CHANGED; EventTime: 258991146; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.FrameLayout; Text: [More options, Copy, Share, Select all, Web search]; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 258991259; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.view.View; Text: []; ContentDescription: null; ItemCount: 0; CurrentItemIndex: 0; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: 0; ScrollY: 0; MaxScrollX: 0; MaxScrollY: 0; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 258991359; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.view.View; Text: []; ContentDescription: null; ItemCount: 0; CurrentItemIndex: 0; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: 0; ScrollY: 0; MaxScrollX: 0; MaxScrollY: 0; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 258992219; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.view.View; Text: []; ContentDescription: null; ItemCount: 0; CurrentItemIndex: 0; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: 0; ScrollY: 0; MaxScrollX: 0; MaxScrollY: 0; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 258992308; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.webkit.WebView; Text: []; ContentDescription: null; ItemCount: 0; CurrentItemIndex: 0; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: true; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: 0; ScrollY: 0; MaxScrollX: 0; MaxScrollY: 3557; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_VIEW_TEXT_SELECTION_CHANGED; EventTime: 258992308; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Publications]; ContentDescription: null; ItemCount: 0; CurrentItemIndex: 0; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: 12; ToIndex: 12; ScrollX: 0; ScrollY: 0; MaxScrollX: 0; MaxScrollY: 0; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
        """.trimIndent()

        assertEquals(true, runEventTest(string))
    }

    @Test
    fun `Chrome "Copy link address"`() {
        val string = """
EventType: TYPE_WINDOW_STATE_CHANGED; EventTime: 245575068; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: at2; Text: []; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: true; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 245578444; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.ListView; Text: []; ContentDescription: null; ItemCount: 8; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: true; BeforeText: null; FromIndex: 0; ToIndex: 0; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 245578516; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.ListView; Text: []; ContentDescription: null; ItemCount: 8; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: 0; ToIndex: 7; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
EventType: TYPE_VIEW_CLICKED; EventTime: 245578516; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Copy link address]; ContentDescription: "Hello word"; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 1
        """.trimIndent()

        assertEquals(true, runEventTest(string))
    }

    private fun runEventTest(string: String): Boolean {
        return EventsHelper.parse(string).map {
            ClipboardDetection.addEvent(it.EventType ?: -1)
            ClipboardDetection.detectAppropriateEvents(event = it, enableLogging = false)
        }.any { it }
    }

}

object EventsHelper {
    private val ArrayRegexPattern = "^\\[(.*)]\$".toRegex()
    private val PropertyRegexPattern = "(\\w+):".toRegex()
    private val ValueRegexPattern = ":\\s?\"?([\\[\\w\\d\\].-]+)\"?".toRegex()
    private val ValueArrayRegexPattern = ":\\s(\\[.*])".toRegex()

    /**
     * Multiline string can be accepting
     */
    fun parse(string: String): List<ClipboardDetection.AEvent> {
        return string.toLines().map { lineParser(it) }
    }

    // eg: EventType: TYPE_VIEW_CLICKED; EventTime: 245578516; PackageName: com.android.chrome; MovementGranularity: 0; Action: 0; ContentChangeTypes: []; WindowChangeTypes: [] [ ClassName: android.widget.TextView; Text: [Copy link address]; ContentDescription: null; ItemCount: -1; CurrentItemIndex: -1; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: -1; ScrollY: -1; MaxScrollX: -1; MaxScrollY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 1
    private fun lineParser(string: String): ClipboardDetection.AEvent {
        val split = string.split(";")
        val e = ClipboardDetection.AEvent()
        for (l in split) {
            when (PropertyRegexPattern.find(l)?.groupValues?.get(1)) {
                ClipboardDetection.AEvent::EventType.name -> e.EventType = getEventType(l)
                ClipboardDetection.AEvent::EventTime.name -> e.EventTime = getValue(l)?.toLong()
                ClipboardDetection.AEvent::PackageName.name -> e.PackageName = getValue(l)
                ClipboardDetection.AEvent::MovementGranularity.name -> e.MovementGranularity =
                    getValue(l)?.toInt()
                ClipboardDetection.AEvent::Action.name -> e.Action = getValue(l)?.toInt()
                ClipboardDetection.AEvent::ClassName.name -> e.ClassName = getValue(l)
                "WindowChangeTypes" -> {
                    val middleware = l.split(" [ ")
                    e.ClassName = getValue(middleware[1])
                }
                ClipboardDetection.AEvent::Text.name -> e.Text = ArrayRegexPattern.find(
                    ValueArrayRegexPattern.find(l)?.groupValues?.get(1) ?: ""
                )?.groupValues?.get(1)?.split(",")
                ClipboardDetection.AEvent::ContentDescription.name -> e.ContentDescription =
                    getValue(l)
                ClipboardDetection.AEvent::CurrentItemIndex.name -> e.CurrentItemIndex =
                    getValue(l)?.toInt()
                ClipboardDetection.AEvent::FromIndex.name -> e.FromIndex = getValue(l)?.toInt()
                ClipboardDetection.AEvent::ToIndex.name -> e.ToIndex = getValue(l)?.toInt()
                ClipboardDetection.AEvent::ScrollX.name -> e.ScrollX = getValue(l)?.toInt()
            }
        }
        return e
    }

    private fun getValue(string: String): String? {
        return ValueRegexPattern.find(string)?.groupValues?.get(1)
    }

    fun getEventType(string: String?): Int? {
        if (string == null) return null
        val value = getValue(string)
        val field = AccessibilityEvent::class.java.fields.find { it.name == value }
        return field?.getInt(null)
    }

}