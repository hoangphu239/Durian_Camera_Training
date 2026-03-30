package com.netsservices.dct.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.ApiResult
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.StatusFile
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.PurposeType
import com.netsservices.dct.presentation.common.toRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private var lastSentTime = 0L

    private var isFlowRunning = false
    private var rawData: ByteArray? = null

    private fun <T> launchApi(
        block: suspend () -> ApiResult<T>,
        onSuccess: (T) -> Unit
    ) {
        viewModelScope.launch {
            if(!_uiState.value.isLoading) {
                _uiState.update { it.copy(isLoading = true) }
            }
            try {
                val result = block()
                result.handle(
                    onSuccess = onSuccess,
                    onError = { _, _ -> clearData() }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun shouldSendFrame(): Boolean {
        val now = System.currentTimeMillis()
        return !isFlowRunning && (now - lastSentTime >= 200)
    }

    @SuppressLint("LogNotTimber")
    fun checkFrame(raw: ByteArray) {
        if (!shouldSendFrame()) return

        rawData = raw
        lastSentTime = System.currentTimeMillis()
        isFlowRunning = true

        viewModelScope.launch {
            try {
                val body = raw.toRequestBody()
                val skipMarkDetection = !PreferenceManager.getFingerprintStatus(context)
                repo.checkFrame(image = body, skipMarkDetection = skipMarkDetection).handle(
                    onSuccess = { data ->
                        _uiState.update { state -> state.copy(dataFrame = data) }
                        if (data.ready && data.durianDetected) {
                            createSession()
                        } else {
                            isFlowRunning = false
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createSession() {
        val site = PreferenceManager.getSite(context)
        val durianId = PreferenceManager.getDurianVariety(context)?.id ?: 0

        val request = CreateSessionRequest(
            purpose = PurposeType.ProfileVerification.name,
            durianTypeId = durianId,
            plantationId = site?.orchard?.plantation?.id ?: "",
            orchardId = site?.orchard?.id ?: "",
            siteId = site?.id ?: ""
        )

        launchApi({ repo.createSessions(request) }) { data ->
            if (data.sessionId.isNotEmpty()) {
                _uiState.update { it.copy(sessionId = data.sessionId) }
                initFile()
            }
        }
    }

    private fun initFile() {
        val sizeBytes = rawData?.size ?: 0
        val request = InitFileRequest(
            purpose = "IMAGE",
            filename = "durian-${System.currentTimeMillis()}.jpg",
            contentType = "image/jpeg",
            sizeBytes = sizeBytes
        )

        launchApi({ repo.initFile(request) }) { data ->
            if (data.fileId.isNotEmpty()) {
                _uiState.update { it.copy(fileId = data.fileId) }
                uploadFile()
            }
        }
    }

    private fun uploadFile() {
        val fileId = _uiState.value.fileId
        val body = rawData?.toRequestBody() ?: return
        launchApi({ repo.uploadFile(fileId, body) }) { data ->
            if (data.fileId.isNotEmpty() && data.status == StatusFile.READY.name) {
                completeFile()
            }
        }
    }

    private fun completeFile() {
        val fileId = _uiState.value.fileId
        launchApi({ repo.completeFile(fileId) }) { data ->
            if (data.fileId.isNotEmpty() && data.status == StatusFile.READY.name) {
                _uiState.update { it.copy(isLoading = false) }
            }
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


