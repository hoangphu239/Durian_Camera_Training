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

enum class ConfigStep {
    SITE,
    MODE,
    DURIAN_TYPE,
    DONE
}

enum class DeviceName(val value: String) {
    PHONE("Phone"),
    THERMAL_CAMERA("Thermal Camera"),
    SOUND_SENSOR("Sound Sensor"),
    NIR("NIR")
}

const val IMAGE_WIDTH = 1080
const val IMAGE_HEIGHT = 1440
