package br.com.zupacademy.william.pixkey

import br.com.zupacademy.william.client.bcb.ChavePixBCBClient
import br.com.zupacademy.william.client.bcb.CreatePixKeyRequest
import br.com.zupacademy.william.client.itau.ItauCustomerAccountClient
import br.com.zupacademy.william.exception.AccountNotFoundException
import br.com.zupacademy.william.exception.PixKeyAlreadyExistsException
import br.com.zupacademy.william.exception.PixKeyNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
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
            bcbClient.criarChave(createPixKeyRequest)
        } catch (exception: HttpClientResponseException) {
//        if (criarChave.status == HttpStatus.UNPROCESSABLE_ENTITY)
            throw PixKeyAlreadyExistsException("A chave '${newPixKeyKey.key}' já foi registrada em outra instituição")
        }

        val chavePix = newPixKeyKey.toModel(possivelConta.body()!!.agency, possivelConta.body()!!.accountNumber)
        return pixKeyRepository.save(chavePix)
    }

    fun delete(idCustomer: String, idPix: Long) {
        val pixKeyNullable = pixKeyRepository.findById(idPix)

        if (pixKeyNullable.isEmpty) {
            throw PixKeyNotFoundException("Chave Pix '$idPix' não encontrada para o cliente '$idCustomer'")
        }

        val pixKey = pixKeyNullable.get()

        pixKeyRepository.deleteByIdCustomerAndPixKeyValue(idCustomer, pixKey.pixKeyValue)

        //TODO job para excluir do bcb chaves que nao foram excluidas na primeira tentativa
        val response = bcbClient.deleteKey(pixKey.pixKeyValue)

        println(response)

    }
}