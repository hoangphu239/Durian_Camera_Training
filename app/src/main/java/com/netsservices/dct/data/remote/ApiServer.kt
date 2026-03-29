package com.netsservices.dct.data.remote

import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.DurianTypeResponse
import com.netsservices.dct.data.remote.response.FileResponse
import com.netsservices.dct.data.remote.response.FringerPrintResponse
import com.netsservices.dct.data.remote.response.InitFileResponse
import com.netsservices.dct.data.remote.response.LoginResponse
import com.netsservices.dct.data.remote.response.OrchardResponse
import com.netsservices.dct.data.remote.response.PlantationResponse
import com.netsservices.dct.data.remote.response.SessionResponse
import com.netsservices.dct.data.remote.response.SiteResponse
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.FingerPrintRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.resquest.LoginRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

const val LOGIN = "/v1/auth/login"
const val QUICK_SEARCH = "/v1/search/sites"
const val SEARCH_PLANTATIONS = "/v1/search/plantations"
const val SEARCH_ORCHARDS = "/v1/search/orchards"
const val SEARCH_SITES = "/v1/search/sites"
const val DURIAN_TYPES = "/v1/durian-types"
const val CHECK_FRAME = "/v1/capture/check-frame"
const val CREATE_SESSIONS = "/v1/session/sessions"
const val INIT_FILE = "/v1/fileroutes/files/init"
const val UPLOAD_FILE = "/v1/fileroutes/files/{fileId}/upload"
const val COMPLETE_FILE = "/v1/fileroutes/files/{fileId}/complete"
const val CREATE_FINGERPRINT = "/v1/session/sessions/{sessionId}/image-fingerprints"

interface ApiServer {

    @POST(LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET(QUICK_SEARCH)
    suspend fun quickSearch(
        @Query("q") query: String
    ): Response<SiteResponse>

    @GET(SEARCH_PLANTATIONS)
    suspend fun searchPlantations(
        @Query("q") query: String
    ): Response<PlantationResponse>

    @GET(SEARCH_ORCHARDS)
    suspend fun searchOrchards(
        @Query("q") query: String,
    ): Response<OrchardResponse>

    @GET(SEARCH_SITES)
    suspend fun searchSites(
        @Query("q") query: String,
    ): Response<SiteResponse>

    @GET(DURIAN_TYPES)
    suspend fun getDurianTypes(
        @Query("countryCode") countryCode: String,
    ): Response<DurianTypeResponse>

    @POST(CHECK_FRAME)
    suspend fun checkFrame(
        @Body image: RequestBody
    ): Response<CheckFrameResponse>

    @POST(CREATE_SESSIONS)
    suspend fun createSessions(
        @Body request: CreateSessionRequest
    ): Response<SessionResponse>

    @POST(INIT_FILE)
    suspend fun initFile(
        @Body request: InitFileRequest
    ): Response<InitFileResponse>

    @PUT(UPLOAD_FILE)
    suspend fun uploadFile(
        @Path("fileId") fileId: String,
        @Body image: RequestBody
    ): Response<FileResponse>

    @POST(COMPLETE_FILE)
    suspend fun completeFile(
        @Path("fileId") fileId: String
    ): Response<FileResponse>

    @POST(CREATE_FINGERPRINT)
    suspend fun createFingerPrint(
        @Path("sessionId") sessionId: String,
        @Body request: FingerPrintRequest
    ): Response<FringerPrintResponse>
}