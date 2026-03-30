package com.netsservices.dct.presentation.common

object Constants {
    const val BASE_URL = "https://mt.duriancare.com/"

    // KEY LANGUAGE
    const val ENGLISH = "en"
    const val THAI = "th"
    const val VIETNAMESE = "vi"
}

enum class PurposeType {
    Prediction,
    ProfileVerification,
    ImageCollection,
    DryMatterCollection
}