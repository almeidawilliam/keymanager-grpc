package br.com.zupacademy.william.pixkey.list

import br.com.zupacademy.william.KeymanagerListGrpcServiceGrpc
import br.com.zupacademy.william.ListRequest
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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixKeyListEndpointTest {

    @field:Inject
    lateinit var pixKeyRepository: PixKeyRepository

    @field:Inject
    lateinit var grpcClient: KeymanagerListGrpcServiceGrpc.KeymanagerListGrpcServiceBlockingStub

    @BeforeEach
    fun setup() {
        pixKeyRepository.deleteAll()
    }

    @Test
    fun `should return three pix keys`() {
        val idCustomer = UUID.randomUUID().toString()

        val pixKey1 = PixKey(
            KeyType.RANDOM,
            UUID.randomUUID().toString(),
            idCustomer,
            AccountType.CONTA_CORRENTE,
            "0001",
            "202123",
            "123456"
        )

        val pixKey2 = PixKey(
            KeyType.PHONE,
            "+159999988888",
            idCustomer,
            AccountType.CONTA_CORRENTE,
            "0001",
            "202123",
            "123456"
        )

        val pixKey3 = PixKey(
            KeyType.CPF,
            "12505338051",
            idCustomer,
            AccountType.CONTA_CORRENTE,
            "0001",
            "202123",
            "123456"
        )

        pixKeyRepository.saveAll(listOf(pixKey1, pixKey2, pixKey3))

        val listRequest = ListRequest.newBuilder()
            .setIdCustomer(idCustomer)
            .build()

        val response = grpcClient.list(listRequest)

        val expectedKeys = listOf(pixKey1, pixKey2, pixKey3)
            .map(PixKey::pixKeyValue)

        assertEquals(expectedKeys, response.pixKeysList.map { key -> key.keyValue })
        assertEquals(listOf(idCustomer), response.pixKeysList.map { idCustomer }.distinct())
        assertEquals(expectedKeys.size, response.pixKeysCount)
    }

    @Test
    fun `should throw exception by keys not found`() {
        val listRequest = ListRequest.newBuilder()
            .setIdCustomer(UUID.randomUUID().toString())
            .build()

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.list(listRequest)
        }

        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertEquals("NOT_FOUND: Chaves Pix não encontrada para o cliente '${listRequest.idCustomer}'", response.message)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeymanagerListGrpcServiceGrpc.KeymanagerListGrpcServiceBlockingStub {

            return KeymanagerListGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}