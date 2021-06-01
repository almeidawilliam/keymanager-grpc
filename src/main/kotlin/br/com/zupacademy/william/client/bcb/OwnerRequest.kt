package br.com.zupacademy.william.client.bcb

data class OwnerRequest(
    val type: PersonType,
    val name: String,
    val taxIdNumber: String
)