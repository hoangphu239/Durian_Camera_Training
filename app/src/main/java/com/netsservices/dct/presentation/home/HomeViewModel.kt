package com.netsservices.dct.presentation.home

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.ApiErrorCode
import com.netsservices.dct.data.remote.ApiErrorCode.CONFLICT
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.DeviceResponse
import com.netsservices.dct.data.remote.response.StatusFile
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.model.DeviceInfo
import com.netsservices.dct.domain.model.Meta
import com.netsservices.dct.domain.model.toRegisterDeviceRequest
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.ConfigStep
import com.netsservices.dct.presentation.common.Constants
import com.netsservices.dct.presentation.common.DeviceName
import com.netsservices.dct.presentation.common.DeviceStatus
import com.netsservices.dct.presentation.common.PurposeType
import com.netsservices.dct.presentation.common.toRequestBody
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.helper.camera.CameraManager
import com.netsservices.dct.presentation.helper.camera.FrameProcessor
import com.netsservices.dct.presentation.helper.connection.NetworkService
import com.netsservices.dct.presentation.utils.Utils.getDeviceID
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.P)
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository,
    networkService: NetworkService,
) : ViewModel() {

    val networkStatus: StateFlow<NetworkService.Status> =
        networkService.networkStatus.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = if (networkService.isInternetAvailable())
                NetworkService.Status.Available
            else
                NetworkService.Status.Lost
        )

    data class UiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val dataFrame: CheckFrameResponse? = null,
        val sessionId: String = "",
        val fileId: String = "",
        val blockCapture: Boolean = false,
        val disconnect: Boolean = false,
        val enableActivation: Boolean = true,
        val message: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var lastSentTime = 0L
    private var isFlowRunning = false
    private var imageBody: RequestBody? = null
    private var gps: Pair<Double, Double>? = null
    private var isUnauthorized = false
    private var latestFrame: ByteArray? = null
    var laserPoints by mutableStateOf<List<Offset>>(emptyList())
    var imageSize by mutableStateOf(IntSize(0, 0))
    private var lastStablePoints: List<Offset> = emptyList()
    private var lastUpdateTime = 0L
    private var stableCount = 0

    fun registerDevice(onResult: () -> Unit) {
        val deviceId = getDeviceID(context)
        val camerasRaw = CameraManager.getAllCameraInfo(context)
        val camerasClean = CameraManager.normalizeCameras(camerasRaw)
        val cameraDetails = CameraManager.buildCameraMeta(camerasClean)

        val deviceInfo = DeviceInfo(
            deviceId = deviceId,
            deviceCode = Build.MODEL,
            name = DeviceName.PHONE.value,
            meta = Meta(
                brand = Build.BRAND,
                cameraDetails = cameraDetails
            )
        )

        viewModelScope.launch {
            repo.registerDevice(deviceInfo.toRegisterDeviceRequest()).handle(
                onSuccess = { data ->
                    if (data.deviceId.isNotEmpty()) {
                        saveDevice(data)
                        updateAction(ConfigStep.REGISTER_DEVICE.name)
                        onResult()
                    }
                },
                onError = { code, _ ->
                    if (code == CONFLICT) {
                        PreferenceManager.saveDeviceStatus(context, DeviceStatus.ACTIVATE.value)
                    }
                    updateAction(ConfigStep.REGISTER_DEVICE.name)
                    onResult()
                }
            )
        }
    }

    fun requestActivation(deviceId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repo.requestActivation(deviceId)
                .handle(
                    onSuccess = { data ->
                        if (data.ok) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    enableActivation = false,
                                    message = data.message
                                )
                            }
                        }
                    },
                    onError = { _, message ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                enableActivation = false,
                                message = message ?: ""
                            )
                        }
                    }
                )
        }
    }

    fun updateLatestFrame(frame: ByteArray) {
        latestFrame = frame
    }

    fun onLaserMatched() {
        if (isFlowRunning) return
        if (uiState.value.blockCapture) return

        latestFrame?.let {
            checkFrame(it)
        }
    }

    fun checkFrame(raw: ByteArray) {
        if (uiState.value.blockCapture) return
        if (isUnauthorized) return
        if (!shouldSendFrame()) return

        viewModelScope.launch {
            val status = networkStatus.first()
            if (status != NetworkService.Status.Available && !_uiState.value.disconnect) {
                _uiState.update { it.copy(disconnect = true) }
                return@launch
            }
        }

        lastSentTime = System.currentTimeMillis()
        isFlowRunning = true

        viewModelScope.launch {
            try {
                val skipMarkDetection =
                    PreferenceManager.getScanMode(context) == ScanMode.COLLECTION
                repo.checkFrame(image = raw.toRequestBody(), skipMarkDetection = skipMarkDetection)
                    .handle(
                        onSuccess = { data ->
                            _uiState.update { state -> state.copy(dataFrame = data) }
                            if (data.ready && data.durianDetected) {
                                imageBody = raw.toRequestBody()
                                initFile(raw.size)
                            } else {
                                isFlowRunning = false
                            }
                        },
                        onError = { code, _ ->
                            if (code == ApiErrorCode.UNAUTHORIZED) {
                                isUnauthorized = true
                            }
                            clearData()
                        }
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initFile(size: Int) {
        val request = InitFileRequest(
            purpose = "IMAGE",
            filename = "durian-${System.currentTimeMillis()}.jpg",
            contentType = "image/jpeg",
            sizeBytes = size
        )

        viewModelScope.launch {
            repo.initFile(request).handle(
                onSuccess = { data ->
                    if (data.fileId.isNotEmpty()) {
                        _uiState.update { it.copy(fileId = data.fileId) }
                        uploadFile()
                    }
                },
                onError = { _, _ -> clearData() }
            )
        }
    }

    private fun uploadFile() {
        val fileId = _uiState.value.fileId
        viewModelScope.launch {
            repo.uploadFile(fileId, imageBody!!).handle(
                onSuccess = { data ->
                    if (data.fileId.isNotEmpty() && data.status == StatusFile.READY.name) {
                        _uiState.update { it.copy(isLoading = false) }
                        createSession()
                    }
                },
                onError = { _, _ -> clearData() }
            )
        }
    }

    private fun createSession() {
        val fileId = uiState.value.fileId
        val durianId = PreferenceManager.getDurianVariety(context)?.id ?: 0
        val contract = if (PreferenceManager.getScanMode(context) == ScanMode.FINGERPRINT) {
            PreferenceManager.getActiveContract(context)
        } else null

        val request = CreateSessionRequest(
            purpose = getPurpose(),
            durianTypeId = durianId,
            plantationId = contract?.plantationId,
            orchardId = contract?.orchardId,
            siteId = contract?.siteId,
            mainFileId = fileId,
            latitude = gps?.first ?: 0.0,
            longitude = gps?.second ?: 0.0,
            contractId = contract?.id
        )

        viewModelScope.launch {
            repo.createSessions(request).handle(
                onSuccess = { data ->
                    if (data.sessionId.isNotEmpty()) {
                        _uiState.update { it.copy(sessionId = data.sessionId, blockCapture = true) }
                        isFlowRunning = false
                    }
                },
                onError = { _, _ -> clearData() }
            )
        }
    }

    fun shouldSendFrame(): Boolean {
        val now = System.currentTimeMillis()
        return !isFlowRunning && (now - lastSentTime >= 200)
    }

    private fun getPurpose(): String {
        val scanMode = PreferenceManager.getScanMode(context)
        return when (scanMode) {
            ScanMode.FINGERPRINT -> PurposeType.ProfileVerification.name
            ScanMode.COLLECTION -> PurposeType.ImageCollection.name
            else -> PurposeType.ProfileVerification.name
        }
    }

    fun unblockCapture() {
        _uiState.update { it.copy(blockCapture = false) }
    }

    fun updateGPS(gps: Pair<Double, Double>) {
        this.gps = gps
    }

    fun updateAction(action: String) {
        PreferenceManager.saveAction(context, action)
    }

    private fun saveDevice(device: DeviceResponse) {
        PreferenceManager.saveDeviceId(context, device.deviceId)
        PreferenceManager.saveDeviceStatus(context, device.status)
    }

    fun getDeviceId(): String = PreferenceManager.getDeviceId(context)

    fun updateLaserPoints(newPoints: List<Offset>) {
        val now = System.currentTimeMillis()

        if (newPoints.size == 3) {
            stableCount++
            if (stableCount >= Constants.REQUIRED_STABLE_FRAMES) {
                lastStablePoints = newPoints
                lastUpdateTime = now
                laserPoints = newPoints
            }
            return
        }

        stableCount = 0
        val holdDuration = 300L

        laserPoints = if (
            lastStablePoints.isNotEmpty() &&
            now - lastUpdateTime < holdDuration
        ) {
            lastStablePoints
        } else {
            emptyList()
        }
    }

    fun clearData() {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSuccess = false,
                dataFrame = null,
                fileId = "",
                sessionId = ""
            )
        }
        isFlowRunning = false
    }
}


