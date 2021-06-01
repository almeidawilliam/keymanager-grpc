package br.com.zupacademy.william.exception

import io.grpc.Status

class PixKeyAlreadyExistsException(override val message: String) : Exception() {
    val status: Status = Status.ALREADY_EXISTS
}
