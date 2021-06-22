package br.com.zupacademy.william.exception

import io.grpc.Status

class AccountNotFoundException(override val message: String) : RuntimeException(), BusinessException {
    override val status: Status = Status.NOT_FOUND
}