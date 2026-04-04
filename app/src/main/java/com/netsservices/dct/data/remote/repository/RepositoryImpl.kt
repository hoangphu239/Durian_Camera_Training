package com.netsservices.dct.data.remote.repository


import com.netsservices.dct.data.remote.ApiResult
import com.netsservices.dct.data.remote.ApiServer
import com.netsservices.dct.data.remote.response.ChangePwdResponse
import com.netsservices.dct.data.remote.response.CheckFrameResponse
import com.netsservices.dct.data.remote.response.ContractResponse
import com.netsservices.dct.data.remote.response.DeviceResponse
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
import com.netsservices.dct.data.remote.resquest.DeviceRequest
import com.netsservices.dct.data.remote.resquest.RegisterRequest
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

    override suspend fun register(registerRequest: RegisterRequest): ApiResult<RegisterResponse> {
        return safeApiCall { api.register(registerRequest) }
    }

    override suspend fun registerDevice(registerRequest: DeviceRequest): ApiResult<DeviceResponse> {
        return safeApiCall { api.registerDevice(registerRequest) }
    }

    override suspend fun changePassword(changeRequest: ChangePwdRequest): ApiResult<ChangePwdResponse> {
        return safeApiCall { api.changePassword(changeRequest) }
    }

    override suspend fun quickSearch(query: String): ApiResult<SiteResponse> {
        return safeApiCall { api.quickSearch(query) }
    }

    override suspend fun getContracts(search: String, status: String): ApiResult<ContractResponse> {
        return safeApiCall { api.getContracts(search, status) }
    }

    override suspend fun getDurianVarieties(countryCode: String): ApiResult<DurianTypeResponse> {
        return safeApiCall { api.getDurianVarieties(countryCode) }
    }

    override suspend fun checkFrame(image: RequestBody, skipMarkDetection: Boolean): ApiResult<CheckFrameResponse> {
        return safeApiCall { api.checkFrame(image, skipMarkDetection) }
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
}


