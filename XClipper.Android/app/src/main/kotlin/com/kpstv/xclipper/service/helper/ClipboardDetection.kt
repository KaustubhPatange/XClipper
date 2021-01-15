package com.kpstv.xclipper.service.helper

import android.view.accessibility.AccessibilityEvent
import androidx.annotation.VisibleForTesting
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.extensions.StripArrayList
import java.util.*

typealias Predicate = (ClipboardDetection.AEvent) -> Boolean

class ClipboardDetection(
    private val copyWord: String = "Copy"
) {

    private val typeViewSelectionChangeEvent: StripArrayList<AEvent> = StripArrayList(2)
    private val eventList: StripArrayList<Int> = StripArrayList(4)
    private var lastEvent: AEvent? = null

    /**
     * Add an [AccessibilityEvent] to the striping array list.
     */
    fun addEvent(c: Int) {
        eventList.add(c)
    }

    /** Some hacks I figured out which would trigger copy/cut for Android 10 */
    fun getSupportedEventTypes(event: AccessibilityEvent?, predicate: Predicate? = null): Boolean {
        if (event == null) return false

        val clipEvent = AEvent.from(event)
        if (predicate?.invoke(clipEvent) == true) return false
        return detectAppropriateEvents(event = clipEvent)
    }

    /**
     * Made this separate function so writing tests can be easy.
     *
     * @param enableLogging Protects the need for mocking [HVLog] in tests.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun detectAppropriateEvents(event: AEvent, enableLogging: Boolean = true): Boolean {
        if (event.EventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            typeViewSelectionChangeEvent.add(event)
        }

        /**
         * This second condition is a hack whenever someone clicks copy or cut context button,
         * it detects this behaviour as copy.
         *
         * Disadvantages: Event TYPE_VIEW_CLICKED is fired whenever you touch on the screen,
         * this means if there is a text which contains "copy" it's gonna consider that as a
         * copy behaviour.
         */
        if (event.EventType == AccessibilityEvent.TYPE_VIEW_CLICKED && event.Text != null
            && (event.ContentDescription?.contains(copyWord, true) == true
                    || event.Text.toString().contains(copyWord, true)
                    || event.ContentDescription == "Cut" || event.ContentDescription == copyWord)
        ) {
            if (enableLogging)
                HVLog.d("Copy captured - 2")
            else
                println("Copy captured - 2")
            return true
        }

        /**
         * We captured the last two [AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED] in list &
         * will try to determine if they are valid for copy action!
         */
        if (typeViewSelectionChangeEvent.size == 2) {
            val firstEvent = typeViewSelectionChangeEvent[0]
            val secondEvent = typeViewSelectionChangeEvent[1]
            if (secondEvent.FromIndex == secondEvent.ToIndex) {
                val success =
                    (firstEvent.PackageName == secondEvent.PackageName && firstEvent.FromIndex != firstEvent.ToIndex
                            && secondEvent.ClassName == firstEvent.ClassName) && secondEvent.Text.toString() == firstEvent.Text.toString()
                typeViewSelectionChangeEvent.clear()
                if (success) {
                    if (enableLogging)
                        HVLog.d("Copy captured - 3")
                    else
                        println("Copy captured - 3")
                    return true
                }
            }
        }

        if (event.ContentChangeTypes ?: 0 and AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE == 1
            && event.EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && lastEvent != null) {
            val previousEvent = lastEvent!!

            if (previousEvent.EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
               /* && previousEvent.ScrollX == -1 && previousEvent.ScrollY == -1*/ // TODO: See if you need any additional checks, uncomment it then
                && (previousEvent.Text?.toString()?.contains(copyWord, true) == true
                || previousEvent.ContentDescription?.contains(copyWord, true) == true)) {
                if (enableLogging)
                    HVLog.d("Copy captured - 1.1")
                else
                    println("Copy captured - 1.1")
                return true
            }
        }

        /**
         * This first condition will allow to capture text from an text selection,
         * whether on chrome or somewhere else.
         *
         * eg: Press and hold a text > a pop comes with different options like
         * copy, paste, select all, etc.
         */

        // Below logic works for many apps but fails on certain apps which makes it literally unusable.
        // It would be better if this is ignored. People can always use quick setting tile to force copy.

        /* if ((event.EventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                     && event.FromIndex == event.ToIndex
                     && (event.PackageName != "com.android.chrome" || event.PackageName != "com.google.android.gm")
                     && event.CurrentItemIndex != -1)
         ) {
             if (event.ClassName == EditText::class.java.name && event.ScrollX != -1) return false
             if (eventList.any { it == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED }) return false
             if (enableLogging)
                 HVLog.d("Copy captured - 1, ToCaptureEvent: ${AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED}, EventList: $eventList")
             return true
         }*/
        lastEvent = event.clone()
        return false
    }

    data class AEvent(
        var EventType: Int? = null,
        var EventTime: Long? = null,
        var PackageName: CharSequence? = null,
        var MovementGranularity: Int? = null,
        var Action: Int? = null,
        var ClassName: CharSequence? = null,
        var Text: List<CharSequence?>? = null,
        var ContentDescription: CharSequence? = null,
        var ContentChangeTypes: Int? = null,
        var CurrentItemIndex: Int? = null,
        var FromIndex: Int? = null,
        var ToIndex: Int? = null,
        var ScrollX: Int? = null,
        var ScrollY: Int? = null,
    ) {
        companion object {
            fun from(event: AccessibilityEvent): AEvent {
                return AEvent(
                    EventType = event.eventType,
                    EventTime = event.eventTime,
                    PackageName = event.packageName,
                    MovementGranularity = event.movementGranularity,
                    Action = event.action,
                    ClassName = event.className,
                    Text = event.text,
                    ContentChangeTypes = event.contentChangeTypes,
                    ContentDescription = event.contentDescription,
                    CurrentItemIndex = event.currentItemIndex,
                    FromIndex = event.fromIndex,
                    ToIndex = event.toIndex,
                    ScrollX = event.scrollX,
                    ScrollY = event.scrollY
                )
            }
        }
    }

    private fun AEvent.clone(): AEvent = this.copy(Text = ArrayList(this.Text ?: listOf()))
}