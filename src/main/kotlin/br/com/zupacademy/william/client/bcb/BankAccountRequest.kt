package br.com.zupacademy.william.client.bcb

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: BCBAccountType?
)