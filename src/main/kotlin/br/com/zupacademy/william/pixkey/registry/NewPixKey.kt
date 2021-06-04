package br.com.zupacademy.william.pixkey.registry

import br.com.zupacademy.william.pixkey.AccountType
import br.com.zupacademy.william.pixkey.KeyType
import br.com.zupacademy.william.pixkey.PixKey
import br.com.zupacademy.william.validation.ValidPixKey
import br.com.zupacademy.william.validation.ValidUUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
class NewPixKey(
    @field:ValidUUID
    @field:NotBlank
    val idCustomer: String,
    @field:NotNull
    val keyType: KeyType,
    @field:Size(max = 77)
    val key: String,
    @field:NotNull
    val accountType: AccountType
) {

    fun toModel(agencia: String, conta: String, ispb: String, keyValue: String): PixKey {
        return PixKey(
            idCustomer = idCustomer,
            pixKeyType = keyType,
            pixKeyValue = keyValue,
            accountType = accountType,
            agency = agencia,
            accountNumber = conta,
            ispb = ispb
        )
    }
}