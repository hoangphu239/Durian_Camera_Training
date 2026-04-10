package com.netsservices.dct.data.remote.resquest

data class ForgotPwdRequest(
    val token: String,
    val newPassword: String
)

