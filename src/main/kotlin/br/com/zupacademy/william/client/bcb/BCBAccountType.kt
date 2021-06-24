package br.com.zupacademy.william.client.bcb

import br.com.zupacademy.william.pixkey.AccountType

enum class BCBAccountType(val accountType: AccountType) {

    CACC(AccountType.CONTA_CORRENTE);

    companion object {

        private val mapping = values().associateBy(BCBAccountType::accountType)

        fun getType(accountType: AccountType): BCBAccountType? {
            return mapping[accountType]
        }
    }

}