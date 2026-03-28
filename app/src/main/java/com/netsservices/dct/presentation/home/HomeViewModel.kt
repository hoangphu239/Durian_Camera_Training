package com.netsservices.dct.presentation.home

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.BuildConfig
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.Orchard
import com.netsservices.dct.data.remote.response.Plantation
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.data.remote.response.StatusFile
import com.netsservices.dct.data.remote.resquest.AuthRequest
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.FingerPrintRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.resquest.QualityChecks
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.SearchMode
import com.netsservices.dct.presentation.common.SearchStep
import com.netsservices.dct.presentation.common.toRequestBody
import com.netsservices.dct.presentation.helper.connection.NetworkService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    networkService: NetworkService,
    private val repo: Repository
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

        val searchMode: SearchMode = SearchMode.SITE,
        val currentStep: SearchStep = SearchStep.SITE,

        val sites: List<Site> = emptyList(),
        val plantations: List<Plantation> = emptyList(),
        val orchards: List<Orchard> = emptyList(),

        val siteId: String = "",
        val plantationId: String = "",
        val orchardId: String = "",

        val dataFrame: CheckFrameResponse? = null,
        val sessionId: String = "",
        val fileId: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var _isLoggedIn by mutableStateOf(false)
    val isLoggedIn: Boolean get() = _isLoggedIn

    private var isGeneratedFringer = false
    private var isChecking = false
    private var lastSentTime = 0L
    private var image: RequestBody? = null
    private var job: Job? = null


    init {
        if (PreferenceManager.getAuthToken(context).isEmpty()) {
            viewModelScope.launch { login() }
        }
    }

    private suspend fun login() {
        val status = networkStatus.first()
        if (status != NetworkService.Status.Available) return

        val response = repo.login(AuthRequest(BuildConfig.AUTH_EMAIL, BuildConfig.AUTH_PASSWORD))
        if (response.token.isNotEmpty()) {
            _isLoggedIn = true
            PreferenceManager.saveAuthToken(context, response.token)
            PreferenceManager.saveUserId(context, response.user.id)
        }
    }

    fun setSearchMode(mode: SearchMode) {
        val step = when (mode) {
            SearchMode.SITE -> SearchStep.SITE
            SearchMode.ORCHARD -> SearchStep.ORCHARD
            SearchMode.PLANTATION -> SearchStep.PLANTATION
        }

        _uiState.update {
            it.copy(
                searchMode = mode,
                currentStep = step,
                sites = emptyList(),
                orchards = emptyList(),
                plantations = emptyList(),
                siteId = "",
                orchardId = "",
                plantationId = ""
            )
        }
    }

    fun searchRouter(query: String) {
        job?.cancel()
        job = viewModelScope.launch {
            delay(100)
            if (query.length >= 2) {
                when (_uiState.value.searchMode) {
                    SearchMode.SITE -> quickSearch(query)
                    SearchMode.ORCHARD -> searchOrchards(query)
                    SearchMode.PLANTATION -> searchPlantations(query)
                }
            }
        }
    }

    fun quickSearch(query: String) {
        viewModelScope.launch {
            delay(100)
            if (query.length >= 2) {
                val response = repo.quickSearch(query)
                _uiState.update { it.copy(sites = response.items) }
            }
        }
    }

    fun searchPlantations(keyword: String) {
        viewModelScope.launch {
            val response = repo.searchPlantations(query = keyword)
            _uiState.update { it.copy(plantations = response.items) }
        }
    }

    fun searchOrchards(keyword: String = "", plantationId: String = "") {
        viewModelScope.launch {
            val response = repo.searchOrchards(query = keyword, plantationId = plantationId)
            _uiState.update { it.copy(orchards = response.items) }
        }
    }

    fun searchSite(keyword: String = "", orchardId: String = "") {
        viewModelScope.launch {
            val response = repo.searchSites(query = keyword, orchardId = orchardId)
            _uiState.update { it.copy(sites = response.items) }
        }
    }

    fun selectPlantation(plantation: Plantation) {
        _uiState.update {
            it.copy(
                plantationId = plantation.id,
                currentStep = SearchStep.ORCHARD,
                orchards = emptyList(),
                sites = emptyList()
            )
        }
        searchOrchards(keyword = "", plantationId = plantation.id)
    }

    fun selectOrchard(orchard: Orchard) {
        _uiState.update {
            it.copy(
                orchardId = orchard.id,
                currentStep = SearchStep.SITE,
                sites = emptyList()
            )
        }
        searchSite(keyword = "", orchardId = orchard.id)
    }

    fun selectSite(site: Site) {
        _uiState.update {
            it.copy(
                siteId = site.id,
                orchardId = site.orchard.id,
                plantationId = site.orchard.plantation.id
            )
        }
    }

    fun shouldSendFrame(): Boolean {
        val now = System.currentTimeMillis()
        return !isChecking &&
                !isGeneratedFringer &&
                (now - lastSentTime >= 1000)
    }

    fun checkFrame(raw: ByteArray) {
        if (!shouldSendFrame()) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            isChecking = true
            lastSentTime = System.currentTimeMillis()
            try {
                image = raw.toRequestBody()
                image?.let {
                    val response = repo.checkFrame(image!!)
//                  if (!response.durianDetected || !response.ready) {
//                      _uiState.update { state -> state.copy(dataFrame = response) }
//                  } else {
                    createSession()
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
            val response = repo.createSessions(request)
            if (response.sessionId.isNotEmpty()) {
                _uiState.update { it.copy(sessionId = response.sessionId) }
                initFile()
            } else {
                clearData()
                Toast.makeText(context, "Create session failure!", Toast.LENGTH_SHORT).show()
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
            val response = repo.initFile(request)
            if (response.fileId.isNotEmpty()) {
                _uiState.update { it.copy(fileId = response.fileId) }
                uploadFile()
            } else {
                clearData()
                Toast.makeText(context, "Init file failure!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFile() {
        viewModelScope.launch {
            val fileId = _uiState.value.fileId
            val response = repo.uploadFile(fileId, image!!)
            if (response.status == StatusFile.READY.name) {
                completeFile()
            } else {
                clearData()
                Toast.makeText(context, "Upload file failure!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun completeFile() {
        viewModelScope.launch {
            val fileId = _uiState.value.fileId
            val response = repo.completeFile(fileId)
            if (response.fileId.isNotEmpty()) {
                createFringerPrint()
            } else {
                clearData()
                Toast.makeText(context, "Complete file failure!", Toast.LENGTH_SHORT).show()
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
            val response = repo.createFingerPrint(sessionId, request)
            if (response.success) {
                _uiState.update { it.copy(isLoading = false) }
                isGeneratedFringer = true
            } else {
                clearData()
                Toast.makeText(context, "Create fringer-print failure!", Toast.LENGTH_SHORT).show()
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


