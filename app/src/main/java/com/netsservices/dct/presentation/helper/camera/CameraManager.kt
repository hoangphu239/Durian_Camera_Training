package com.netsservices.dct.presentation.helper.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.util.SizeF
import androidx.annotation.RequiresApi
import com.netsservices.dct.domain.model.CameraDetail
import com.netsservices.dct.domain.model.SensorInfo
import com.netsservices.dct.presentation.common.ULTRA_WIDE
import com.netsservices.dct.presentation.common.WIDE
import com.netsservices.dct.presentation.common.TELE
import com.netsservices.dct.presentation.common.UNKNOWN
import com.netsservices.dct.presentation.common.BACK

object CameraManager {

    enum class CameraType {
        ULTRA_WIDE,
        WIDE,
        TELE,
        FRONT,
        UNKNOWN
    }

    data class CameraInfo(
        val cameraId: String,
        val type: CameraType,
        val facing: Int?,
        val focal: Float?,
        val sensorSize: SizeF?,
        val isMacroCapable: Boolean
    )

    fun getCameraType(
        facing: Int?,
        focal: Float?
    ): CameraType {

        if (facing == CameraMetadata.LENS_FACING_FRONT) {
            return CameraType.FRONT
        }

        if (focal == null) return CameraType.UNKNOWN

        return when {
            focal < 2.5f -> CameraType.ULTRA_WIDE
            focal < 6f -> CameraType.WIDE
            else -> CameraType.TELE
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getAllCameraInfo(context: Context): List<CameraInfo> {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        val result = mutableListOf<CameraInfo>()

        try {
            for (cameraId in manager.cameraIdList) {
                val chars = manager.getCameraCharacteristics(cameraId)

                val facing = chars.get(CameraCharacteristics.LENS_FACING)

                val focal = chars
                    .get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.firstOrNull()

                val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)

                val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                val isMacro = afModes?.contains(CameraMetadata.CONTROL_AF_MODE_MACRO) == true

                val type = getCameraType(facing, focal)

                result.add(
                    CameraInfo(
                        cameraId = cameraId,
                        type = type,
                        facing = facing,
                        focal = focal,
                        sensorSize = sensorSize,
                        isMacroCapable = isMacro
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    fun normalizeCameras(cameras: List<CameraInfo>): List<CameraInfo> {
        return cameras
            .groupBy { "${it.facing}-${it.focal}" }
            .map { (_, group) ->
                group.maxByOrNull { it.sensorSize?.width ?: 0f }!!
            }
    }


    fun buildCameraMeta(cameras: List<CameraInfo>): CameraDetail {
        val mainCamera = cameras
            .filter { it.facing == CameraMetadata.LENS_FACING_BACK }
            .maxByOrNull { it.sensorSize?.width ?: 0f }

        val cameraDetail = mainCamera?.let { cam ->

            val type = when (cam.type) {
                CameraType.ULTRA_WIDE -> ULTRA_WIDE
                CameraType.WIDE -> WIDE
                CameraType.TELE -> TELE
                else -> UNKNOWN
            }

            CameraDetail(
                facing = BACK,
                type = type,
                focalLengthMm = cam.focal?.toDouble() ?: 0.0,
                sensor = SensorInfo(
                    widthMm = cam.sensorSize?.width?.toDouble() ?: 0.0,
                    heightMm = cam.sensorSize?.height?.toDouble() ?: 0.0
                ),
            )
        }
        return cameraDetail?: CameraDetail()
    }
}