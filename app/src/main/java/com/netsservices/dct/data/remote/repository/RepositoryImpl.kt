package com.netsservices.dct.data.remote.repository


import com.netsservices.dct.data.remote.ApiResult
import com.netsservices.dct.data.remote.ApiServer
import com.netsservices.dct.data.remote.response.LoginResponse
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.FileResponse
import com.netsservices.dct.data.remote.response.FringerPrintResponse
import com.netsservices.dct.data.remote.response.InitFileResponse
import com.netsservices.dct.data.remote.response.OrchardResponse
import com.netsservices.dct.data.remote.response.PlantationResponse
import com.netsservices.dct.data.remote.response.SessionResponse
import com.netsservices.dct.data.remote.response.SiteResponse
import com.netsservices.dct.data.remote.resquest.LoginRequest
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.FingerPrintRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.safeApiCall
import com.netsservices.dct.domain.repository.Repository
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RepositoryImpl @Inject constructor(
    private val api: ApiServer,
) : Repository {

    override suspend fun login(request: LoginRequest): ApiResult<LoginResponse> {
        return safeApiCall { api.login(request) }
    }

    override suspend fun quickSearch(query: String): ApiResult<SiteResponse> {
        return safeApiCall { api.quickSearch(query) }
    }

    override suspend fun searchPlantations(query: String): ApiResult<PlantationResponse> {
        return safeApiCall { api.searchPlantations(query) }
    }

    override suspend fun searchOrchards(query: String): ApiResult<OrchardResponse> {
        return safeApiCall { api.searchOrchards(query) }
    }

    override suspend fun searchSites(query: String): ApiResult<SiteResponse> {
        return safeApiCall { api.searchSites(query) }
    }

    override suspend fun checkFrame(image: RequestBody): ApiResult<CheckFrameResponse> {
        return safeApiCall { api.checkFrame(image) }
    }

    override suspend fun createSessions(request: CreateSessionRequest): ApiResult<SessionResponse> {
        return safeApiCall { api.createSessions(request) }
    }

    override suspend fun initFile(request: InitFileRequest): ApiResult<InitFileResponse> {
        return safeApiCall { api.initFile(request) }
    }

    override suspend fun uploadFile(
        fileId: String,
        image: RequestBody
    ): ApiResult<FileResponse> {
        return safeApiCall { api.uploadFile(fileId, image) }
    }

    override suspend fun completeFile(fileId: String): ApiResult<FileResponse> {
        return safeApiCall { api.completeFile(fileId) }
    }

    override suspend fun createFingerPrint(
        sessionId: String,
        request: FingerPrintRequest
    ): ApiResult<FringerPrintResponse> {
        return safeApiCall { api.createFingerPrint(sessionId, request) }
    }
}


