package com.netsservices.dct.data.remote

import retrofit2.Response
import org.json.JSONObject

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(response.code(), "Empty response body")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                JSONObject(errorBody ?: "").getString("message")
            } catch (e: Exception) {
                response.message()
            }
            ApiResult.Error(response.code(), errorMessage ?: "Unknown error")
        }
    } catch (e: Exception) {
        ApiResult.Exception(e)
    }
}