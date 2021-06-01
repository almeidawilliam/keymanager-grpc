package br.com.zupacademy.william.chavepix

import io.micronaut.validation.validator.constraints.EmailValidator
//import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class KeyType {

    CPF {
        override fun validate(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            if (!chave.matches("[0-9]+".toRegex())) {
                return false
            }

            return true

//            TODO - Corrigir import da dependencia do validator
//            return CPFValidator().run {
//                initialize(null)
//                isValid(chave, null)
//            }
        }
    },
    PHONE {
        override fun validate(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun validate(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    RANDOM {
        override fun validate(chave: String?) = chave.isNullOrBlank()
    };

    abstract fun validate(chave: String?): Boolean
}