package com.netsservices.dct.domain.repository

import com.netsservices.dct.data.remote.ApiResult
import com.netsservices.dct.data.remote.response.ChangePwdResponse
import com.netsservices.dct.data.remote.response.CheckFrameResponse
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
import com.netsservices.dct.data.remote.resquest.RegisterDeviceRequest
import com.netsservices.dct.data.remote.resquest.RegisterRequest
import okhttp3.RequestBody


interface Repository {
    suspend fun login(request: LoginRequest): ApiResult<LoginResponse>
    suspend fun register(registerRequest: RegisterRequest): ApiResult<RegisterResponse>
    suspend fun registerDevice(registerRequest: RegisterDeviceRequest): ApiResult<DeviceResponse>
    suspend fun changePassword(changeRequest: ChangePwdRequest): ApiResult<ChangePwdResponse>
    suspend fun quickSearch(query: String): ApiResult<SiteResponse>
    suspend fun getDurianVarieties(countryCode: String): ApiResult<DurianTypeResponse>
    suspend fun checkFrame(image: RequestBody, skipMarkDetection: Boolean): ApiResult<CheckFrameResponse>
    suspend fun createSessions(request: CreateSessionRequest): ApiResult<SessionResponse>
    suspend fun initFile(request: InitFileRequest): ApiResult<InitFileResponse>
    suspend fun uploadFile(fileId: String, image: RequestBody): ApiResult<FileResponse>
}