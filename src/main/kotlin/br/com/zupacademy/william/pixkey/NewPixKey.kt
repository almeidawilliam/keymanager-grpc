package br.com.zupacademy.william.pixkey

import br.com.zupacademy.william.validation.ValidPixKey
import br.com.zupacademy.william.validation.ValidUUID
import java.util.*
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

    fun toModel(agencia: String, conta: String): PixKey {
        return PixKey(
            idCustomer = idCustomer,
            pixKeyType = keyType,
            pixKeyValue = if (this.keyType == KeyType.RANDOM) UUID.randomUUID().toString() else this.key,
            accountType = accountType,
            agency = agencia,
            accountNumber = conta
        )
    }
}