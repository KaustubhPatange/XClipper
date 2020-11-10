package com.kpstv.xclipper.service.helper

import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import androidx.annotation.VisibleForTesting
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.extensions.StripArrayList
import java.util.*

object ClipboardDetection {

    private val eventList: StripArrayList<Int> =
        StripArrayList(4) // TODO: Try to fix it by stripping 4 to 3

    /** Some hacks I figured out which would trigger copy/cut for Android 10 */
    fun getSupportedEventTypes(event: AccessibilityEvent?): Boolean {
        return detectAppropriateEvents(
            eventType = event?.eventType,
            eventText = event?.text,
            eventContentDescription = event?.contentDescription,
            eventClassName = event?.className,
            fromIndex = event?.fromIndex,
            toIndex = event?.toIndex,
            currentIndex = event?.currentItemIndex,
            scrollX = event?.scrollX
        )
    }

    /**
     * Add an [AccessibilityEvent] to the striping array list.
     */
    fun addEvent(c: Int) {
        eventList.add(c)
    }

    /**
     * Made this separate function so writing tests can be easy.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun detectAppropriateEvents(
        eventType: Int?,
        eventText: MutableList<CharSequence?>?,
        eventContentDescription: CharSequence?,
        eventClassName: CharSequence?,
        fromIndex: Int?,
        toIndex: Int?,
        currentIndex: Int?,
        scrollX: Int?,
        enableLogging: Boolean = true
    ): Boolean {
        /**
         * This second condition is a hack whenever someone clicks copy or cut context button,
         * it detects this behaviour as copy.
         *
         * Disadvantages: Event TYPE_VIEW_CLICKED is fired whenever you touch on the screen,
         * this means if there is a text which contains "copy" it's gonna consider that as a
         * copy behaviour.
         */
        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED && eventText != null
            && (eventContentDescription?.toString()?.toLowerCase(Locale.ROOT)?.contains("copy") == true
                    || eventText.toString().toLowerCase(Locale.ROOT).contains("copy")
                    || eventContentDescription == "Cut" || eventContentDescription == "Copy")
        ) {
            if (enableLogging)
                HVLog.d("Copy captured - 2")
            return true
        }

        /**
         * This first condition will allow to capture text from an text selection,
         * whether on chrome or somewhere else.
         *
         * eg: Press and hold a text > a pop comes with different options like
         * copy, paste, select all, etc.
         */
        if ((eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                    && fromIndex == toIndex
                    && currentIndex != -1)
        ) {
            if (eventClassName == EditText::class.java.name && scrollX != -1) return false
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
}