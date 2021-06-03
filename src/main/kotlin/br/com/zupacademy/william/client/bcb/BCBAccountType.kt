package br.com.zupacademy.william.client.bcb

import br.com.zupacademy.william.pixkey.AccountType

enum class BCBAccountType(val accountType: AccountType) {

    CACC(AccountType.CONTA_CORRENTE);

    companion object {
        fun getType(accountType: AccountType): BCBAccountType? {
            for (type: BCBAccountType in values()) {
                if (type.accountType == accountType) {
                    return type
                }
            }
            return null
        }
    }

}