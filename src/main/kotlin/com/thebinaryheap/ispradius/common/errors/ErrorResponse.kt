package com.thebinaryheap.ispradius.common.errors

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String
)