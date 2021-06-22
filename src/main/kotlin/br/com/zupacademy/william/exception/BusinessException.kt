package br.com.zupacademy.william.exception

import io.grpc.Status

interface BusinessException {
    val status: Status
    val message: String
}