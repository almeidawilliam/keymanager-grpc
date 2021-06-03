package br.com.zupacademy.william.pixkey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {

    fun existsByPixKeyValue(key: String): Boolean
    fun deleteByIdCustomerAndPixKeyValue(idCustomer: String, keyValue: String)
}