package br.com.zupacademy.william.chavepix

import br.com.zupacademy.william.KeymanagerGrpcRequest

fun KeymanagerGrpcRequest.toModel() : NewPixKey {
    return NewPixKey(
        idCustomer = idCliente,
        keyType = KeyType.valueOf(tipoChave.name),
        accountType = AccountType.valueOf(tipoConta.name),
        key = valorChave
    )
}