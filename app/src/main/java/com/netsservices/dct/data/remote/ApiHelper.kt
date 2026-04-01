package com.netsservices.dct.data.remote

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
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

@SuppressLint("LogNotTimber")
inline fun <T> ApiResult<T>.handle(
    onSuccess: (T) -> Unit,
    noinline onError: ((code: Int, message: String?) -> Unit) = { _, _ -> }
) {
    when (this) {
        is ApiResult.Success -> onSuccess(data)
        is ApiResult.Error -> {
            onError(code, message)
            handleError(code, message ?: "Unknown error")
        }
        is ApiResult.Exception -> {
            onError(-1, exception.localizedMessage)
            Log.e("API-Exception", exception.localizedMessage ?: "Network error")
        }
    }
}

fun handleError(errorCode: Int, message: String) {
    when (errorCode) {
        ApiErrorCode.UNAUTHORIZED -> {
            CoroutineScope(Dispatchers.Main).launch {
                AppEventBus.events.emit(AppEvent.Unauthorized(message))
            }
        }

        ApiErrorCode.FORBIDDEN -> {
            AppEvent.ShowToast("You don’t have permission")
        }

        ApiErrorCode.TIMEOUT -> {
            AppEvent.ShowToast("Request timeout, please try again")
        }

        ApiErrorCode.SERVER_ERROR -> {
            AppEvent.ShowToast("Server error, try again later")
        }

        else -> {
            AppEvent.ShowToast(message)
        }
    }
}

object AppEventBus {
    val events = MutableSharedFlow<AppEvent>()
}

sealed class AppEvent {
    data class ShowToast(val message: String) : AppEvent()
    data class Unauthorized(val message: String) : AppEvent()
}