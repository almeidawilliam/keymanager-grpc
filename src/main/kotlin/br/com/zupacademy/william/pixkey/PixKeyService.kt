package br.com.zupacademy.william.pixkey

import br.com.zupacademy.william.*
import br.com.zupacademy.william.AccountType
import br.com.zupacademy.william.KeyType
import br.com.zupacademy.william.client.bcb.ChavePixBCBClient
import br.com.zupacademy.william.client.bcb.CreatePixKeyRequest
import br.com.zupacademy.william.client.bcb.DeletePixKeyRequest
import br.com.zupacademy.william.client.itau.ItauCustomerAccountClient
import br.com.zupacademy.william.exception.AccountNotFoundException
import br.com.zupacademy.william.exception.PixKeyAlreadyExistsException
import br.com.zupacademy.william.exception.PixKeyNotFoundException
import br.com.zupacademy.william.pixkey.registry.NewPixKey
import com.google.protobuf.Timestamp
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class PixKeyService(
    @field:Inject val pixKeyRepository: PixKeyRepository,
    @field:Inject val itauCustomerAccountClient: ItauCustomerAccountClient,
    @field:Inject val bcbClient: ChavePixBCBClient
) {

    fun save(@Valid newPixKey: NewPixKey): PixKey {
        if (pixKeyRepository.existsByPixKeyValue(newPixKey.key)) {
            throw PixKeyAlreadyExistsException("Chave pix '${newPixKey.key}' já está cadastrada")
        }

        val possivelConta =
            itauCustomerAccountClient.findCustomerAccount(newPixKey.idCustomer, newPixKey.accountType)

        if (possivelConta.body() == null) {
            throw AccountNotFoundException("'${newPixKey.accountType}' não encontrada para cliente '${newPixKey.idCustomer}'")
        }

        val createPixKeyRequest = CreatePixKeyRequest(newPixKey, possivelConta.body()!!)

//        TODO - HttpClientResponseException nao para de ser lançada na stacktrace
        try {
            val criarChave = bcbClient.criarChave(createPixKeyRequest)

            val chavePix = newPixKey.toModel(
                possivelConta.body()!!.agency,
                possivelConta.body()!!.accountNumber,
                possivelConta.body()!!.institution.ispb,
                criarChave.body()!!.key
            )

            return pixKeyRepository.save(chavePix)

        } catch (exception: HttpClientResponseException) {
//        if (criarChave.status == HttpStatus.UNPROCESSABLE_ENTITY)
            throw PixKeyAlreadyExistsException("A chave '${newPixKey.key}' já foi registrada em outra instituição")
        }
    }

    fun delete(idCustomer: String, idPix: Long) {
        val pixKey = pixKeyRepository.findById(idPix)
            .orElseThrow { PixKeyNotFoundException("Chave Pix '$idPix' não encontrada para o cliente '$idCustomer'") }

        pixKeyRepository.deleteByIdCustomerAndPixKeyValue(idCustomer, pixKey.pixKeyValue)

        /* TODO
        *       - job para excluir do bcb chaves que nao foram excluidas na primeira tentativa
        *       - try catch ou devolver httpresponse para tratar 4xx
        */
        val deletePixKeyRequest = DeletePixKeyRequest(pixKey.pixKeyValue, pixKey.ispb)
        bcbClient.deleteKey(pixKey.pixKeyValue, deletePixKeyRequest)

    }

    fun find(idPix: Long, idCustomer: String): FindResponse {
        val pixKey = pixKeyRepository.findByIdAndIdCustomer(idPix, idCustomer)
            .orElseThrow { PixKeyNotFoundException("Chave Pix '$idPix' não encontrada para o cliente '$idCustomer'") }

        /* TODO
         *      - tratar chave nao registrada no bcb
         */
//        val response = bcbClient.findKey(pixKey.pixKeyValue)

//        if (response.status == HttpStatus.OK) {
//
//        }
        val accountNullable =
            itauCustomerAccountClient.findCustomerAccount(pixKey.idCustomer, pixKey.accountType)

        if (accountNullable.status != HttpStatus.OK) {
            throw AccountNotFoundException("'${pixKey.accountType}' não encontrada para cliente '${pixKey.idCustomer}'")
        }

        val customerAccountResponse = accountNullable.body()!!

        return FindResponse.newBuilder()
            .setIdPix(pixKey.id!!)
            .setKeyType(KeyType.valueOf(pixKey.pixKeyType.name))
            .setKeyValue(pixKey.pixKeyValue)
            .setCustomer(
                Customer.newBuilder()
                    .setIdCustomer(customerAccountResponse.accountHolder.id)
                    .setCpf(customerAccountResponse.accountHolder.cpf)
                    .setName(customerAccountResponse.accountHolder.nome)
                    .build()
            )
            .setCustomerAccount(
                CustomerAccount.newBuilder()
                    .setInstitutionName(customerAccountResponse.institution.nome)
                    .setAccountType(AccountType.valueOf(customerAccountResponse.accountType.name))
                    .setAgency(customerAccountResponse.agency)
                    .setAccount(customerAccountResponse.accountNumber)
                    .build()
            )
            .setCreatedAt(
                com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(pixKey.createdAt.toEpochSecond(ZoneOffset.UTC))
                    .setNanos(pixKey.createdAt.nano)
                    .build()
            )
            .build()
    }

    fun list(idCustomer: String): ListResponse {
        val pixKeys = pixKeyRepository.findAllByIdCustomer(idCustomer)
            .map { pixKey ->
                br.com.zupacademy.william.PixKey.newBuilder()
                    .setIdPix(pixKey.id!!)
                    .setKeyType(KeyType.valueOf(pixKey.pixKeyType.name))
                    .setKeyValue(pixKey.pixKeyValue)
                    .setAccountType(AccountType.valueOf(pixKey.accountType.name))
                    .setCreatedAt(
                        Timestamp.newBuilder()
                            .setSeconds(pixKey.createdAt.toEpochSecond(ZoneOffset.UTC))
                            .setNanos(pixKey.createdAt.nano)
                            .build()
                    )
                    .build()
            }
            .toList()

        if (pixKeys.isEmpty()) {
            throw PixKeyNotFoundException("Chaves Pix não encontrada para o cliente '$idCustomer'")
        }

        return ListResponse.newBuilder()
            .setIdCustomer(idCustomer)
            .addAllPixKeys(pixKeys)
            .build()
    }
}