package com.netsservices.dct.data.remote

import com.netsservices.dct.data.remote.response.BundleLanguageResponse
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.DurianTypeResponse
import com.netsservices.dct.data.remote.response.FileResponse
import com.netsservices.dct.data.remote.response.InitFileResponse
import com.netsservices.dct.data.remote.response.LoginResponse
import com.netsservices.dct.data.remote.response.RegisterResponse
import com.netsservices.dct.data.remote.response.SessionResponse
import com.netsservices.dct.data.remote.response.SiteResponse
import com.netsservices.dct.data.remote.resquest.ChangePwdRequest
import com.netsservices.dct.data.remote.resquest.CreateSessionRequest
import com.netsservices.dct.data.remote.resquest.InitFileRequest
import com.netsservices.dct.data.remote.resquest.LoginRequest
import com.netsservices.dct.data.remote.response.ChangePwdResponse
import com.netsservices.dct.data.remote.response.ContractResponse
import com.netsservices.dct.data.remote.response.CountryResponse
import com.netsservices.dct.data.remote.response.DeviceResponse
import com.netsservices.dct.data.remote.response.ForgotPwdResponse
import com.netsservices.dct.data.remote.response.LanguageResponse
import com.netsservices.dct.data.remote.response.RequestActivationResponse
import com.netsservices.dct.data.remote.resquest.DeviceRequest
import com.netsservices.dct.data.remote.resquest.ForgotPwdRequest
import com.netsservices.dct.data.remote.resquest.RegisterRequest
import com.netsservices.dct.domain.model.Country
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


const val LOGIN = "/v1/auth/login"
const val REGISTER = "/v1/auth/register"
const val CHANGE_PASSWORD = "/v1/auth/change-password"
const val FORGOT_PASSWORD = "/v1/auth/forgot-password"
const val COUNTRIES = "/v1/countries"
const val LANGUAGES = "/v1/lang/languages"
const val LANGUAGE_BUNDLE = "/v1/lang/bundle"
const val CONTRACTS = "/v1/contracts"
const val REGISTER_DEVICE = "/v1/device/register"
const val QUICK_SEARCH = "/v1/search/sites"
const val DURIAN_TYPES = "/v1/durian-types"
const val REQUEST_ACTIVATION = "/v1/device/{deviceId}/request-activation"
const val CHECK_FRAME = "/v1/capture/check-frame"
const val CREATE_SESSIONS = "/v1/session/sessions"
const val INIT_FILE = "/v1/fileroutes/files/init"
const val UPLOAD_FILE = "/v1/fileroutes/files/{fileId}/upload"

interface ApiServer {

    @POST(REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST(LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET(LANGUAGE_BUNDLE)
    suspend fun getBundleLanguage(
        @Query("langId") langId: String,
        @Query("prefix") prefix: String
    ): Response<BundleLanguageResponse>

    @GET(LANGUAGES)
    suspend fun getLanguages(): Response<List<LanguageResponse>>

    @GET(COUNTRIES)
    suspend fun getCountries(): Response<CountryResponse>

    @POST(FORGOT_PASSWORD)
    suspend fun forgotPassword(
        @Body request: ForgotPwdRequest
    ): Response<ForgotPwdResponse>


    @POST(CHANGE_PASSWORD)
    suspend fun changePassword(
        @Body request: ChangePwdRequest
    ): Response<ChangePwdResponse>

    @POST(REGISTER_DEVICE)
    suspend fun registerDevice(
        @Body request: DeviceRequest
    ): Response<DeviceResponse>

    @GET(QUICK_SEARCH)
    suspend fun quickSearch(
        @Query("q") query: String
    ): Response<SiteResponse>

    @GET(CONTRACTS)
    suspend fun getContracts(
        @Query("search") search: String,
        @Query("status") status: String
    ): Response<ContractResponse>

    @GET(DURIAN_TYPES)
    suspend fun getDurianVarieties(
        @Query("countryCode") countryCode: String?,
    ): Response<DurianTypeResponse>

    @POST(REQUEST_ACTIVATION)
    suspend fun requestActivation(
        @Path("deviceId") deviceId: String,
    ): Response<RequestActivationResponse>

    @POST(CHECK_FRAME)
    suspend fun checkFrame(
        @Body image: RequestBody,
        @Query("skipMarkDetection") skipMarkDetection: Boolean
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
}