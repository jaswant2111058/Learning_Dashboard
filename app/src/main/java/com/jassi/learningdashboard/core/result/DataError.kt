package com.jassi.learningdashboard.core.result

sealed interface DataError {
    data object Network : DataError

    data class Server(val code: Int) : DataError

    data object Unauthorized : DataError

    data object Serialization : DataError

    data class Unknown(val message: String?) : DataError
}
