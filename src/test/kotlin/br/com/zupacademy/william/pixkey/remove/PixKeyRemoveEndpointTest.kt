package br.com.zupacademy.william.pixkey.remove

import br.com.zupacademy.william.KeymanagerRemoveServiceGrpc
import br.com.zupacademy.william.RemoveRequest
import br.com.zupacademy.william.client.bcb.ChavePixBCBClient
import br.com.zupacademy.william.pixkey.AccountType
import br.com.zupacademy.william.pixkey.KeyType
import br.com.zupacademy.william.pixkey.PixKey
import br.com.zupacademy.william.pixkey.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixKeyRemoveEndpointTest {

    @field:Inject
    lateinit var pixKeyRepository: PixKeyRepository

    @field:Inject
    lateinit var grpcClient: KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub

    @Test
    fun `should delete registered key`() {
        val pixKey = PixKey(
            KeyType.PHONE,
            "+5515981486622",
            UUID.randomUUID().toString(),
            AccountType.CONTA_CORRENTE,
            "0001",
            "12345",
            "1515"
        )

        val pixKeySaved = pixKeyRepository.save(pixKey)

        val removeRequest = RemoveRequest.newBuilder()
            .setIdPix(pixKeySaved.id!!)
            .setIdCustomer(pixKeySaved.idCustomer)
            .build()

        grpcClient.remove(removeRequest)

        assertEquals(0, pixKeyRepository.count())
    }

    @Test
    fun `should throw exception by key not found for customer`() {
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.remove(mock(RemoveRequest::class.java))
        }

        assertEquals("NOT_FOUND: Chave Pix '0' não encontrada para o cliente ''", response.message)
        assertEquals(Status.NOT_FOUND.code, response.status.code)
    }

    @MockBean(ChavePixBCBClient::class)
    fun mockChavePixBCBClient() = mock(ChavePixBCBClient::class.java)

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub {
            return KeymanagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }
}