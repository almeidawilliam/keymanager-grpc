package br.com.zupacademy.william.chavepix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {

    fun existsByPixKeyValue(chave: String): Boolean
}