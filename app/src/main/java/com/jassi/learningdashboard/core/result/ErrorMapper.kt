package com.jassi.learningdashboard.core.result

import com.jassi.learningdashboard.data.auth.AuthException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toDataError(): DataError = when (this) {
    is AuthException -> if (code == HTTP_UNAUTHORIZED) DataError.Unauthorized else DataError.Server(code)
    is HttpException -> if (code() == HTTP_UNAUTHORIZED) DataError.Unauthorized else DataError.Server(code())
    is SerializationException -> DataError.Serialization
    is IOException -> DataError.Network
    else -> DataError.Unknown(message)
}

private const val HTTP_UNAUTHORIZED = 401
