package com.netsservices.dct.presentation.common

object Constants {
    const val BASE_URL = "https://api.duriancare.com/"
    const val INVALID_TOKEN = "Invalid token"
    const val ULTRA_WIDE = "ULTRA_WIDE"
    const val WIDE = "WIDE"
    const val TELE = "TELE"
    const val UNKNOWN = "UNKNOWN"
    const val BACK = "BACK"
    const val CAMERA_TARGET_WIDTH = 1080
    const val CAMERA_TARGET_HEIGHT = 1920
    const val TRIANGLE_SIZE_RATIO = 0.4f // chiều dài cạnh tam giác

    const val TOLERANCE_RATIO = 0.45f
    const val CIRCLE_RADIUS_RATIO = 0.12f
    const val STROKE_WIDTH = 4f
    val VIBRATION_PATTERN = longArrayOf(0, 150, 200, 150)
    const val REQUIRED_STABLE_FRAMES = 3

    const val MAX_LUMA = 70f
    // Ngưỡng độ sáng (luminance)
    // ↓ giảm xuống → chỉ nhận màu rất tối (đen hơn)
    // ↑ tăng lên → chấp nhận màu sáng hơn (nhạt hơn)

    const val MIN_RGB_SUM = 75
    // loại bỏ đen tuyệt đối (shadow sâu)
    // ↓ giảm → cho phép đen hơn
    // ↑ tăng → loại bỏ vùng quá đen

    const val DENSITY_RADIUS = 5    // bán kính vùng kiểm tra cụm pixel

    const val MIN_DARK_DENSITY = 0.45f
    // tỉ lệ pixel "nâu đen" cần đạt
    // ↓ giảm → dễ detect hơn (nhưng dễ nhiễu)
    // ↑ tăng → khó hơn (nhưng chính xác hơn)
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

enum class GuidanceState {
    TOO_FAR,
    TOO_CLOSE,
    GOOD,
    NOT_FOUND
}

