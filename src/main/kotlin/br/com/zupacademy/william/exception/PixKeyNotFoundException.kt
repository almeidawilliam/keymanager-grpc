package br.com.zupacademy.william.exception

import io.grpc.Status

class PixKeyNotFoundException(override val message: String) : RuntimeException(), BusinessException {
    override val status: Status = Status.NOT_FOUND
}