package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager

class KeyboardUtils {
    companion object {

        /**
         * Determines if keyboard visible using below logic.
         *
         * Assuming standard keyboard height must be greater than 100 px.
         */
        fun isKeyboardVisible(context: Context): Boolean {
            return getKeyboardHeight(context) > 100
        }

        /**
         * The method detects the height of IMM window.
         *
         * Originally the method uses #getInputMethodWindowVisibleHeight from IMM,
         * but recently due to a bug report they hide it internally.
         *
         * Thanks to this guy https://stackoverflow.com/a/52171843/10133501, who made
         * me aware that we can invoke this method using reflections.
         */
        fun getKeyboardHeight(context: Context): Int {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            val windowHeightMethod = InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")

            /** I assume the keyboard size is at least greater than 100px */
            return try {
                windowHeightMethod.invoke(imm) as Int
            } catch (e: Exception) {
                0
            }
        }
    }
}