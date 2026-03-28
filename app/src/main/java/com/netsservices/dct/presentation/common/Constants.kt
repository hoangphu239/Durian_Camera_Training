package com.netsservices.dct.presentation.common

object Constants {
    const val BASE_URL = "https://duriancareapi.netsservices.dk/"

    // KEY LANGUAGE
    const val ENGLISH = "en"
    const val THAI = "th"
    const val VIETNAMESE = "vi"
}

enum class SearchMode {
    SITE,
    ORCHARD,
    PLANTATION
}

enum class SearchStep {
    PLANTATION,
    ORCHARD,
    SITE
}