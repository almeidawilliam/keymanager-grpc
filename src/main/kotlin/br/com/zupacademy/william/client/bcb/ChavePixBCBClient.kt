package br.com.zupacademy.william.client.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082/api/v1/pix/keys")
interface ChavePixBCBClient {

    @Post(produces = [MediaType.APPLICATION_XML])
    fun criarChave(@Body request: CreatePixKeyRequest)

    @Delete(produces = [MediaType.APPLICATION_XML])
    fun deleteKey(@PathVariable key: String): HttpResponse<Any>
}