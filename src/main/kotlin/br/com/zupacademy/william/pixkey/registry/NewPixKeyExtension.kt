package br.com.zupacademy.william.pixkey

import br.com.zupacademy.william.RegistryRequest
import br.com.zupacademy.william.pixkey.registry.NewPixKey

fun RegistryRequest.toModel() : NewPixKey {
    return NewPixKey(
        idCustomer = idCliente,
        keyType = KeyType.valueOf(tipoChave.name),
        accountType = AccountType.valueOf(tipoConta.name),
        key = valorChave
    )
}