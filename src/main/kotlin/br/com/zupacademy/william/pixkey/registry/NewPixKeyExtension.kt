package br.com.zupacademy.william.pixkey.registry

import br.com.zupacademy.william.RegistryRequest
import br.com.zupacademy.william.pixkey.AccountType
import br.com.zupacademy.william.pixkey.KeyType

fun RegistryRequest.toModel() : NewPixKey {
    return NewPixKey(
        idCustomer = idCustomer,
        keyType = KeyType.valueOf(keyType.name),
        accountType = AccountType.valueOf(accountType.name),
        key = keyValue
    )
}