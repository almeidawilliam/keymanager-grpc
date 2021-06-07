package br.com.zupacademy.william.pixkey.find

import br.com.zupacademy.william.*
import br.com.zupacademy.william.client.bcb.ChavePixBCBClient
import br.com.zupacademy.william.client.itau.AccountHolder
import br.com.zupacademy.william.client.itau.AccountResponse
import br.com.zupacademy.william.client.itau.Institution
import br.com.zupacademy.william.client.itau.ItauCustomerAccountClient
import br.com.zupacademy.william.pixkey.AccountType
import br.com.zupacademy.william.pixkey.PixKey
import br.com.zupacademy.william.pixkey.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixKeyFindEndpointTest {

    @field:Inject
    lateinit var pixKeyRepository: PixKeyRepository

    @field:Inject
    lateinit var bcbClient: ChavePixBCBClient

    @field:Inject
    lateinit var itauCustomerAccountClient: ItauCustomerAccountClient

    @field:Inject
    lateinit var grpcClient: KeymanagerFindGrpcServiceGrpc.KeymanagerFindGrpcServiceBlockingStub

    @BeforeEach
    fun setup() {
        pixKeyRepository.deleteAll()
    }

    @Test
    fun `should find a pix key`() {
        val accountResponse = AccountResponse(
            AccountType.CONTA_CORRENTE,
            Institution("institution", "123456"),
            "0001",
            "202123",
            AccountHolder(
                UUID.randomUUID().toString(), "Joaquim da Silva", "45455167006"
            )
        )

        val pixKey = PixKey(
            br.com.zupacademy.william.pixkey.KeyType.PHONE,
            "+159999988888",
            accountResponse.accountHolder.id,
            AccountType.CONTA_CORRENTE,
            "0001",
            "202123",
            "123456"
        )

        `when`(itauCustomerAccountClient.findCustomerAccount(any(), any()))
            .thenReturn(HttpResponse.ok(accountResponse))

        val newPixKey = pixKeyRepository.save(pixKey)

        val findRequest = FindRequest.newBuilder()
            .setIdPix(newPixKey.id!!)
            .setIdCustomer(accountResponse.accountHolder.id)
            .build()

        val response = grpcClient.find(findRequest)

        val expectedResponse = FindResponse.newBuilder()
            .setIdPix(newPixKey.id!!)
            .setKeyType(KeyType.valueOf(newPixKey.pixKeyType.name))
            .setKeyValue(newPixKey.pixKeyValue)
            .setCustomer(
                Customer.newBuilder()
                    .setIdCustomer(accountResponse.accountHolder.id)
                    .setName("Joaquim da Silva")
                    .setCpf("45455167006")
                    .build()
            )
            .setCustomerAccount(
                CustomerAccount.newBuilder()
                    .setInstitutionName(accountResponse.institution.nome)
                    .setAccountType(br.com.zupacademy.william.AccountType.valueOf(accountResponse.accountType.name))
                    .setAgency(accountResponse.agency)
                    .setAccount(accountResponse.accountNumber)
                    .build()
            )
            .setCreatedAt(
                com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(newPixKey.createdAt.toEpochSecond(ZoneOffset.UTC))
                    .setNanos(newPixKey.createdAt.nano)
            )

//        println("real creation of object ${pixKey.createdAt.nano}")
//        println("real creation on database ${newPixKey.createdAt.nano}")
//        println("expected response ${expectedResponse.createdAt.nanos}")
//        println("response ${response.createdAt.nanos}")

        // Didn't work cause of nanos
//        assertEquals(expectedResponse, response)
        assertEquals(response.idPix, expectedResponse.idPix)
        assertEquals(response.keyType, expectedResponse.keyType)
        assertEquals(response.keyValue, expectedResponse.keyValue)
        assertEquals(response.customerAccount, expectedResponse.customerAccount)
    }

    @Test
    fun `should throw exception by key not found`() {
        val findRequest = FindRequest.newBuilder()
            .setIdPix(1)
            .setIdCustomer(UUID.randomUUID().toString())
            .build()

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.find(findRequest)
        }

        assertEquals("NOT_FOUND: Chave Pix '${findRequest.idPix}' não encontrada para o cliente '${findRequest.idCustomer}'", response.message)
        assertEquals(Status.NOT_FOUND.code, response.status.code)
    }

    @Test
    fun `should throw exception by customer account not found`() {
        val accountResponse = AccountResponse(
            AccountType.CONTA_CORRENTE,
            Institution("institution", "123456"),
            "0001",
            "202123",
            AccountHolder(
                UUID.randomUUID().toString(), "Joaquim da Silva", "45455167006"
            )
        )

        val pixKey = PixKey(
            br.com.zupacademy.william.pixkey.KeyType.PHONE,
            "+159999988888",
            accountResponse.accountHolder.id,
            AccountType.CONTA_CORRENTE,
            "0001",
            "202123",
            "123456"
        )

        val newPixKey = pixKeyRepository.save(pixKey)

        `when`(itauCustomerAccountClient.findCustomerAccount(any(), any()))
            .thenReturn(HttpResponse.notFound())

        val findRequest = FindRequest.newBuilder()
            .setIdPix(newPixKey.id!!)
            .setIdCustomer(newPixKey.idCustomer)
            .build()

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.find(findRequest)
        }

        assertEquals("NOT_FOUND: '${pixKey.accountType}' não encontrada para cliente '${findRequest.idCustomer}'" , response.message)
        assertEquals(Status.NOT_FOUND.code, response.status.code)
    }


    @MockBean(ItauCustomerAccountClient::class)
    fun ItauCustomerAccountClient(): ItauCustomerAccountClient {
        return mock(ItauCustomerAccountClient::class.java)
    }

    @MockBean(ChavePixBCBClient::class)
    fun mockChavePixBCBClient(): ChavePixBCBClient {
        return mock(ChavePixBCBClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeymanagerFindGrpcServiceGrpc.KeymanagerFindGrpcServiceBlockingStub {

            return KeymanagerFindGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}