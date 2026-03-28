package com.netsservices.dct.presentation.helper.connection

import kotlinx.coroutines.flow.Flow

interface NetworkService {
    val networkStatus: Flow<Status>
    fun isInternetAvailable(): Boolean

    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}