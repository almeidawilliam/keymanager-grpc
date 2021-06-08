package br.com.zupacademy.william.client.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082/api/v1/pix/keys")
interface ChavePixBCBClient {

    @Get("/{key}", produces = [MediaType.APPLICATION_XML])
    fun findKey(@PathVariable key: String): HttpResponse<Any>

    @Post(produces = [MediaType.APPLICATION_XML])
    fun registryKey(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/{key}", produces = [MediaType.APPLICATION_XML])
    fun deleteKey(@PathVariable key: String, @Body deletePixKeyRequest: DeletePixKeyRequest): HttpResponse<Any>
}