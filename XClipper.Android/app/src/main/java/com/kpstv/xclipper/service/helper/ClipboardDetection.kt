package com.kpstv.xclipper.service.helper

import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import androidx.annotation.VisibleForTesting
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.extensions.StripArrayList
import java.util.*

typealias Predicate = (ClipboardDetection.AEvent) -> Boolean

object ClipboardDetection {

    private val typeViewSelectionChangeEvent: StripArrayList<AEvent> = StripArrayList(2)
    private val eventList: StripArrayList<Int> = StripArrayList(4)

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
            && (event.ContentDescription?.toString()?.toLowerCase(Locale.ROOT)
                ?.contains("copy") == true
                    || event.Text.toString().toLowerCase(Locale.ROOT).contains("copy")
                    || event.ContentDescription == "Cut" || event.ContentDescription == "Copy")
        ) {
            if (enableLogging)
                HVLog.d("Copy captured - 2")
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
                    if (enableLogging) HVLog.d("Copy captured - 3")
                    return true
                }
            }
        }

        /**
         * This first condition will allow to capture text from an text selection,
         * whether on chrome or somewhere else.
         *
         * eg: Press and hold a text > a pop comes with different options like
         * copy, paste, select all, etc.
         */
        if ((event.EventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                    && event.FromIndex == event.ToIndex
                    && event.CurrentItemIndex != -1)
        ) {
            if (event.ClassName == EditText::class.java.name && event.ScrollX != -1) return false
            /** I don't know what Gmail is doing, but it cast the EditText class as TextView
             *  when someone is writing, due to which above event fails to exclude it.
             *  This should prevent it.
             */
            if (eventList.any { it == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED }) return false
            if (enableLogging)
                HVLog.d("Copy captured - 1, ToCaptureEvent: ${AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED}, EventList: $eventList")
            return true
        }
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
        var CurrentItemIndex: Int? = null,
        var FromIndex: Int? = null,
        var ToIndex: Int? = null,
        var ScrollX: Int? = null
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
                    ContentDescription = event.contentDescription,
                    CurrentItemIndex = event.currentItemIndex,
                    FromIndex = event.fromIndex,
                    ToIndex = event.toIndex,
                    ScrollX = event.scrollX
                )
            }
        }
    }
}