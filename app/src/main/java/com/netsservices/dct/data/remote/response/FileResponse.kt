package com.netsservices.dct.data.remote.response

data class FileResponse(
    val fileId: String,
    val status: String = StatusFile.INIT.name,
)

enum class StatusFile {
    INIT, READY
}
