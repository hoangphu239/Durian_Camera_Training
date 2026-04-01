package com.netsservices.dct.data.remote.resquest

data class ChangePwdRequest(
    val currentPassword: String,
    val newPassword: String
)