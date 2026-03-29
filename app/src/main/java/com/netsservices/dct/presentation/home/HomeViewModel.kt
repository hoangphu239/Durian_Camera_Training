package com.netsservices.dct.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.ApiResult
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.StatusFile
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.FingerPrintRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.resquest.QualityChecks
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.showToast
import com.netsservices.dct.presentation.common.toRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val dataFrame: CheckFrameResponse? = null,
        val sessionId: String = "",
        val fileId: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var isGeneratedFringer = false
    private var isChecking = false
    private var lastSentTime = 0L
    private var image: RequestBody? = null


    fun shouldSendFrame(): Boolean {
        val now = System.currentTimeMillis()
        return !isChecking && (now - lastSentTime >= 200)
    }

    fun checkFrame(raw: ByteArray) {
        if (!shouldSendFrame()) return

        viewModelScope.launch {
            isChecking = true
            lastSentTime = System.currentTimeMillis()

            try {
                val body = raw.toRequestBody()
                when (val result = repo.checkFrame(body)) {
                    is ApiResult.Success -> {
                        _uiState.update { state -> state.copy(dataFrame = result.data) }
                        if (result.data.ready && result.data.durianDetected) {
                            createSession()
                        }
                    }

                    is ApiResult.Error -> {
                        context.showToast(result.message ?: "Unknown error")
                    }

                    is ApiResult.Exception -> {
                        context.showToast("Network error: ${result.exception.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isChecking = false
            }
        }
    }

    private fun createSession(
        purpose: String = "Prediction",
        specimenId: String = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        plantationId: String = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        orchardId: String = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        siteId: String = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        profileId: String = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        fruitTag: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val request = CreateSessionRequest(
                purpose = purpose,
                specimenId = specimenId,
                plantationId = plantationId,
                orchardId = orchardId,
                siteId = siteId,
                profileId = profileId,
                fruitTag = fruitTag,
                notes = notes
            )
            when (val result = repo.createSessions(request)) {
                is ApiResult.Success -> {
                    if (result.data.sessionId.isNotEmpty()) {
                        _uiState.update { it.copy(sessionId = result.data.sessionId) }
                        initFile()
                    }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                    clearData()
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                    clearData()
                }
            }
        }
    }

    private fun initFile(
        purpose: String = "IMAGE",
        filename: String = "durian-${System.currentTimeMillis()}.jpg",
        contentType: String = "image/jpeg",
        sizeBytes: Int = 1,
        sha256: String = ""
    ) {
        viewModelScope.launch {
            val request = InitFileRequest(
                purpose = purpose,
                filename = filename,
                contentType = contentType,
                sizeBytes = sizeBytes,
                sha256 = sha256
            )
            when (val result = repo.initFile(request)) {
                is ApiResult.Success -> {
                    if (result.data.fileId.isNotEmpty()) {
                        _uiState.update { it.copy(fileId = result.data.fileId) }
                        uploadFile()
                    }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                    clearData()
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                    clearData()
                }
            }
        }
    }

    private fun uploadFile() {
        viewModelScope.launch {
            val fileId = _uiState.value.fileId
            when (val result = repo.uploadFile(fileId, image!!)) {
                is ApiResult.Success -> {
                    if (result.data.status == StatusFile.READY.name) {
                        completeFile()
                    }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                    clearData()
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                    clearData()
                }
            }
        }
    }

    private fun completeFile() {
        viewModelScope.launch {
            val fileId = _uiState.value.fileId

            when (val result = repo.completeFile(fileId)) {
                is ApiResult.Success -> {
                    if (result.data.fileId.isNotEmpty()) {
                        createFringerPrint()
                    }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                    clearData()
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                    clearData()
                }
            }
        }
    }

    private fun createFringerPrint() {
        viewModelScope.launch {
            val sessionId = _uiState.value.sessionId
            val request = FingerPrintRequest(
                mainFileId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                capturedAt = "2026-03-28T04:34:26.886Z",
                deviceId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                deviceLabel = "",
                scanPosition = "SPOT_1",
                measurementTarget = "WHOLE_FRUIT",
                markerProfileId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                markerProfileCode = "",
                plantationId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                orchardId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                siteId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                fruitTag = "",
                operatorUserId = "string",
                appVersion = "1.0.0",
                cameraModel = "",
                imageWidth = 1,
                imageHeight = 1,
                distanceMm = 0f,
                qualityScore = 1f,
                qualityChecks = QualityChecks(),
                notes = ""
            )

            when (val result = repo.createFingerPrint(sessionId, request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false) }
                        isGeneratedFringer = true
                    }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                    clearData()
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                    clearData()
                }
            }
        }
    }

    fun clearData() {
        _uiState.update {
            it.copy(
                dataFrame = null,
                isLoading = false,
                fileId = "",
                sessionId = ""
            )
        }
        isGeneratedFringer = false
    }
}


