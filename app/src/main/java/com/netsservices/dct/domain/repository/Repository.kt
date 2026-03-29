package com.netsservices.dct.domain.repository

import com.netsservices.dct.data.remote.ApiResult
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
import okhttp3.RequestBody


interface Repository {
    suspend fun login(request: LoginRequest): ApiResult<LoginResponse>
    suspend fun quickSearch(query: String): ApiResult<SiteResponse>
    suspend fun searchPlantations(query: String): ApiResult<PlantationResponse>
    suspend fun searchOrchards(query: String): ApiResult<OrchardResponse>
    suspend fun searchSites(query: String): ApiResult<SiteResponse>
    suspend fun checkFrame(image: RequestBody): ApiResult<CheckFrameResponse>
    suspend fun createSessions(request: CreateSessionRequest): ApiResult<SessionResponse>
    suspend fun initFile(request: InitFileRequest): ApiResult<InitFileResponse>
    suspend fun uploadFile(fileId: String, image: RequestBody): ApiResult<FileResponse>
    suspend fun completeFile(fileId: String): ApiResult<FileResponse>
    suspend fun createFingerPrint(
        sessionId: String,
        request: FingerPrintRequest
    ): ApiResult<FringerPrintResponse>
}