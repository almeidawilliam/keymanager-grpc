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

    fun save(@Valid newPixKeyKey: NewPixKey): PixKey {
        if (pixKeyRepository.existsByPixKeyValue(newPixKeyKey.key)) {
            throw PixKeyAlreadyExistsException("Chave pix '${newPixKeyKey.key}' já está cadastrada")
        }

        val possivelConta =
            itauCustomerAccountClient.findCustomerAccount(newPixKeyKey.idCustomer, newPixKeyKey.accountType)

        if (possivelConta.body() == null) {
            throw AccountNotFoundException("'${newPixKeyKey.accountType}' não encontrada para cliente '${newPixKeyKey.idCustomer}'")
        }

        val createPixKeyRequest = CreatePixKeyRequest(newPixKeyKey, possivelConta.body()!!)

//        TODO - HttpClientResponseException nao para de ser lançada na stacktrace
        try {
            val criarChave = bcbClient.criarChave(createPixKeyRequest)

            val chavePix = newPixKeyKey.toModel(
                possivelConta.body()!!.agency,
                possivelConta.body()!!.accountNumber,
                possivelConta.body()!!.institution.ispb,
                criarChave.body()!!.key
            )

            return pixKeyRepository.save(chavePix)

        } catch (exception: HttpClientResponseException) {
//        if (criarChave.status == HttpStatus.UNPROCESSABLE_ENTITY)
            throw PixKeyAlreadyExistsException("A chave '${newPixKeyKey.key}' já foi registrada em outra instituição")
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

        val customerAccountResponse = itauCustomerAccountClient.findCustomerAccount(idCustomer, pixKey.accountType)

        return FindResponse.newBuilder()
            .setIdPix(pixKey.id!!)
            .setKeyType(KeyType.valueOf(pixKey.pixKeyType.name))
            .setKeyValue(pixKey.pixKeyValue)
            .setCustomer(
                Customer.newBuilder()
                    .setIdCustomer(customerAccountResponse.body()!!.accountHolder.id)
                    .setCpf(customerAccountResponse.body()!!.accountHolder.cpf)
                    .setName(customerAccountResponse.body()!!.accountHolder.nome)
                    .build()
            )
            .setCustomerAccount(
                CustomerAccount.newBuilder()
                    .setInstitutionName(customerAccountResponse.body()!!.institution.nome)
                    .setAccountType(AccountType.valueOf(customerAccountResponse.body()!!.accountType.name))
                    .setAgency(customerAccountResponse.body()!!.agency)
                    .setAccount(customerAccountResponse.body()!!.accountNumber)
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
}