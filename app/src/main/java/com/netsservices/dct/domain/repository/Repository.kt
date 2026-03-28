package com.netsservices.dct.domain.repository

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
import okhttp3.RequestBody


interface Repository {
    suspend fun login(request: AuthRequest): AuthResponse
    suspend fun quickSearch(query: String): SiteResponse
    suspend fun searchPlantations(query: String): PlantationResponse
    suspend fun searchOrchards(query: String, plantationId: String): OrchardResponse
    suspend fun searchSites(query: String, orchardId: String): SiteResponse
    suspend fun checkFrame(image: RequestBody): CheckFrameResponse
    suspend fun createSessions(request: CreateSessionRequest): SessionResponse
    suspend fun initFile(request: InitFileRequest): InitFileResponse
    suspend fun uploadFile(fileId: String, image: RequestBody): FileResponse
    suspend fun completeFile(fileId: String): FileResponse
    suspend fun createFingerPrint(
        sessionId: String,
        request: FingerPrintRequest
    ): FringerPrintResponse
}