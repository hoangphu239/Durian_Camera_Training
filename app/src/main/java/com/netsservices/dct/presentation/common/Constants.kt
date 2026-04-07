package com.netsservices.dct.presentation.common

object Constants {
    const val BASE_URL = "https://api.duriancare.com/"
}

enum class PurposeType {
    Prediction,
    ProfileVerification,
    ImageCollection,
    DryMatterCollection
}

enum class ConfigStep {
    REGISTER_DEVICE,
    SITE,
    MODE,
    DURIAN_TYPE,
    DONE
}

enum class DeviceStatus(val value: String) {
    UNACTIVE("Unactive"),
    PENDING_APPROVAL("Pending"),
    ACTIVATE("Active")
}

enum class DeviceName(val value: String) {
    PHONE("Phone"),
    THERMAL_CAMERA("Thermal Camera"),
    SOUND_SENSOR("Sound Sensor"),
    NIR("NIR")
}

enum class ContractStatus(val value: String) {
    DRAFT("Draft"),
    PENDING("PendingApproval"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
}

const val IMAGE_WIDTH = 1920
const val IMAGE_HEIGHT = 1080
const val INVALID_TOKEN = "Invalid token"
const val ULTRA_WIDE = "ULTRA_WIDE"
const val WIDE = "WIDE"
const val TELE = "TELE"
const val UNKNOWN = "UNKNOWN"
const val BACK = "BACK"
