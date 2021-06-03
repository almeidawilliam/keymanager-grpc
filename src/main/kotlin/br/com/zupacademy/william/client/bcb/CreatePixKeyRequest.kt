package br.com.zupacademy.william.client.bcb

import br.com.zupacademy.william.client.itau.AccountResponse
import br.com.zupacademy.william.pixkey.KeyType
import br.com.zupacademy.william.pixkey.NewPixKey

data class CreatePixKeyRequest(
    val NewPixKey: NewPixKey,
    val accountResponse: AccountResponse
) {

    val keyType: KeyType = NewPixKey.keyType
    val key: String = NewPixKey.key

    val bankAccount: BankAccountRequest = BankAccountRequest(
        participant = accountResponse.institution.ispb,
        branch = accountResponse.agency,
        accountNumber = accountResponse.accountNumber,
        accountType = BCBAccountType.getType(accountResponse.accountType)
    )

    val owner: OwnerRequest = OwnerRequest(
        PersonType.NATURAL_PERSON,
        accountResponse.accountHolder.nome,
        accountResponse.accountHolder.cpf
    )
}