package com.netsservices.dct.data.remote.response

data class RegisterResponse(
    val id: String,
    val email: String,
    val role: String,
    val status: String,
    val createdAt: String
)
