package com.wwt.core.data

sealed class DataState

data class SuccessResult<T>(
    val data: T? = null
) : DataState()

data class FailureResult<T>(val error: T? = null) : DataState()