package com.jassi.learningdashboard.core.result

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: DataError) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onFailure(block: (DataError) -> Unit): AppResult<T> = apply {
    if (this is AppResult.Failure) block(error)
}
