package br.com.zupacademy.william.pixkey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {

    fun existsByPixKeyValue(key: String): Boolean
    fun deleteByIdCustomerAndPixKeyValue(idCustomer: String, keyValue: String)
    fun findByIdAndIdCustomer(idPix: Long, idCustomer: String): Optional<PixKey>
    fun findAllByIdCustomer(idCustomer: String): List<PixKey>
}