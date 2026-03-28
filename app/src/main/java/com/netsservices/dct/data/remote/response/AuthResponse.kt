package com.netsservices.dct.data.remote.response

import com.google.gson.annotations.SerializedName
import com.netsservices.dct.domain.model.User

data class AuthResponse(
    @SerializedName("accessToken")
    val token: String,
    @SerializedName("user")
    val user: User
)