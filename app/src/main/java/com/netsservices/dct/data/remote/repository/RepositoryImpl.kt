package com.netsservices.dct.data.remote.repository


import com.netsservices.dct.data.remote.ApiServer
import com.netsservices.dct.data.remote.response.AuthResponse
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.FileResponse
import com.netsservices.dct.data.remote.response.FringerPrintResponse
import com.netsservices.dct.data.remote.response.InitFileResponse
import com.netsservices.dct.data.remote.response.OrchardResponse
import com.netsservices.dct.data.remote.response.PlantationResponse
import com.netsservices.dct.data.remote.response.SessionResponse
import com.netsservices.dct.data.remote.response.SiteResponse
import com.netsservices.dct.data.remote.resquest.AuthRequest
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.FingerPrintRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.domain.repository.Repository
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RepositoryImpl @Inject constructor(
    private val api: ApiServer,
) : Repository {

    override suspend fun login(request: AuthRequest): AuthResponse {
        return api.login(request)
    }

    override suspend fun quickSearch(query: String): SiteResponse {
        return api.quickSearch(query)
    }

    override suspend fun searchPlantations(query: String): PlantationResponse {
        return api.searchPlantations(query)
    }

    override suspend fun searchOrchards(
        query: String,
        plantationId: String
    ): OrchardResponse {
        return api.searchOrchards(query, plantationId)
    }

    override suspend fun searchSites(
        query: String,
        orchardId: String
    ): SiteResponse {
        return api.searchSites(query, orchardId)
    }

    override suspend fun checkFrame(image: RequestBody): CheckFrameResponse {
        return api.checkFrame(image)
    }

    override suspend fun createSessions(request: CreateSessionRequest): SessionResponse {
        return api.createSessions(request)
    }

    override suspend fun initFile(request: InitFileRequest): InitFileResponse {
        return api.initFile(request)
    }

    override suspend fun uploadFile(
        fileId: String,
        image: RequestBody
    ): FileResponse {
        return api.uploadFile(fileId, image)
    }

    override suspend fun completeFile(fileId: String): FileResponse {
        return api.completeFile(fileId)
    }

    override suspend fun createFingerPrint(
        sessionId: String,
        request: FingerPrintRequest
    ): FringerPrintResponse {
        return api.createFingerPrint(sessionId, request)
    }
}


