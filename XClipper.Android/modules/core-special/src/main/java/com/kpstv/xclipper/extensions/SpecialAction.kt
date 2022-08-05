package com.kpstv.xclipper.extensions

enum class SpecialAction {
    SEARCH_QUERY, SEARCH_MAP, SET_CALENDER_EVENT, SEND_MAIL, PHONE_CALL, CREATE_CONTACT, TEXT_NUMBER, TEXT_WHATSAPP, OPEN_LINK, OPEN_PRIVATE, SHORTEN_LINK, QR_CODE;
/*TODO: Add translation, currency converter */
    companion object {
        fun all(): List<SpecialAction> = values().toList()
    }
}