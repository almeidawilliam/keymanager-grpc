package br.com.zupacademy.william.client.itau

import br.com.zupacademy.william.pixkey.AccountType
import com.fasterxml.jackson.annotation.JsonProperty

data class AccountResponse(
    @JsonProperty("tipo")
    val accountType: AccountType,
    @JsonProperty("instituicao")
    val institution: Institution,
    @JsonProperty("agencia")
    val agency: String,
    @JsonProperty("numero")
    val accountNumber: String,
    @JsonProperty("titular")
    val accountHolder: AccountHolder
)