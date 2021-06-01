package br.com.zupacademy.william.validation

import br.com.zupacademy.william.chavepix.NewPixKey
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "chave pix inválida (\${validatedValue.tipoChave})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = []
)

@Singleton
class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, NewPixKey> {

    override fun isValid(
        value: NewPixKey?,
        context: ConstraintValidatorContext?
    ): Boolean {

        if (value?.keyType == null) {
            return false
        }

        return value.keyType.validate(value.key)
    }
}