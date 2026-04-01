package com.netsservices.dct.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.StatusFile
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.PurposeType
import com.netsservices.dct.presentation.common.toRequestBody
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.helper.connection.NetworkService
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
        val disconnect: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private var lastSentTime = 0L

    private var isFlowRunning = false
    private var imageBody: RequestBody? = null

    fun shouldSendFrame(): Boolean {
        val now = System.currentTimeMillis()
        return !isFlowRunning && (now - lastSentTime >= 200)
    }

    @SuppressLint("LogNotTimber")
    fun checkFrame(raw: ByteArray) {
        if (uiState.value.blockCapture) return
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
                val skipMarkDetection = PreferenceManager.getScanMode(context) == ScanMode.COLLECTION
                repo.checkFrame(image = raw.toRequestBody(), skipMarkDetection = skipMarkDetection).handle(
                    onSuccess = { data ->
                        _uiState.update { state -> state.copy(dataFrame = data) }
                        if (data.ready && data.durianDetected) {
                            imageBody = raw.toRequestBody()
                            initFile(raw.size)
                        } else {
                            isFlowRunning = false
                        }
                    },
                    onError = { _, _ ->
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
        val site = PreferenceManager.getSite(context)
        val durianId = PreferenceManager.getDurianVariety(context)?.id ?: 0
        val fileId = uiState.value.fileId

        val request = CreateSessionRequest(
            purpose = PurposeType.ProfileVerification.name,
            durianTypeId = durianId,
            plantationId = site?.orchard?.plantation?.id ?: "",
            orchardId = site?.orchard?.id ?: "",
            siteId = site?.id ?: "",
            fileId = fileId
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

    fun unblockCapture() {
        _uiState.update { it.copy(blockCapture = false) }
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


