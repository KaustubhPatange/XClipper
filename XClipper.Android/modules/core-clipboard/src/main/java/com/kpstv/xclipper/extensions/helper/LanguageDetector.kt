package com.kpstv.xclipper.extensions.helper

import android.content.Context
import android.os.Build

object LanguageDetector {
    fun getCopyForLocale(context: Context): String {
        val lang = find(context)
        return baseMap.getOrElse(lang) { "Copy" }
    }

    @Suppress("DEPRECATION")
    fun find(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            context.resources.configuration.locale.language
        }
    }

    /**
     * I can use localazy to automate translation but its quite tiresome
     * instead I'll use the translation that I need to detect copy action.
     */
    private val baseMap = mutableMapOf(
        "en" to "Copy",
        "af" to "Kopieer",
        "am" to "ቅዳ",
        "ar" to "نسخ",
        "as" to "প্ৰতিলিপি কৰক",
        "az" to "Kopyalayın",
        "sr" to "Kopiraj",
        "be" to "Капіраваць",
        "bg" to "Копиране",
        "bn" to "কপি করুন",
        "bs" to "Kopiraj",
        "ca" to "Copia",
        "cs" to "Kopírovat",
        "da" to "Kopiér",
        "de" to "Kopieren",
        "el" to "Αντιγραφή",
        "es" to "Copiar",
        "et" to "Kopeerimine",
        "eu" to "Kopiatu",
        "fa" to "کپی",
        "fi" to "Kopioi",
        "fr" to "Copier",
        "gl" to "Copiar",
        "gu" to "કૉપિ કરો",
        "hi" to "कॉपी करें",
        "hr" to "Kopiraj",
        "hu" to "Másolás",
        "hy" to "Պատճենել",
        "in" to "Salin",
        "is" to "Afrita",
        "it" to "Copia",
        "iw" to "העתקה",
        "ja" to "コピー",
        "ka" to "კოპირება",
        "kk" to "Көшіру",
        "km" to "ចម្លង",
        "kn" to "ನಕಲಿಸಿ",
        "ko" to "복사",
        "ky" to "Көчүрүү",
        "lo" to "ສຳເນົາ",
        "lt" to "Kopijuoti",
        "lv" to "Kopēt",
        "mk" to "Копирај",
        "m1" to "പകർത്തുക",
        "mn" to "Хуулах",
        "mr" to "कॉपी करा",
        "ms" to "Salin",
        "my" to "မိတ္တူကူးရန်",
        "nb" to "Kopiér",
        "ne" to "प्रतिलिपि गर्नुहोस्",
        "nl" to "Kopiëren",
        "or" to "କପି କରନ୍ତୁ",
        "pa" to "ਕਾਪੀ ਕਰੋ",
        "pl" to "Kopiuj",
        "pt" to "Copiar",
        "ro" to "Copiați",
        "ru" to "Копировать",
        "si" to "පිටපත් කරන්න",
        "sk" to "Kopírovať",
        "sl" to "Kopiraj",
        "sq" to "Kopjo",
        "sr" to "Копирај",
        "sv" to "Kopiera",
        "sw" to "Nakili",
        "ta" to "நகலெடு",
        "te" to "కాపీ చేయి",
        "th" to "คัดลอก",
        "tl" to "Kopyahin",
        "tr" to "Kopyala",
        "uk" to "Скопіювати",
        "ur" to "کاپی کریں",
        "uz" to "Nusxa olish",
        "vi" to "Sao chép",
        "zh" to "复制",
        "zh-CN" to "复制",
        "zh-HK" to "複製",
        "zh-TW" to "複製",
        "zu" to "Kopisha"
    )
}