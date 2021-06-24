package br.com.zupacademy.william.validation.interceptor

import br.com.zupacademy.william.ErrorBody
import br.com.zupacademy.william.ErrorDetails
import br.com.zupacademy.william.exception.AccountNotFoundException
import br.com.zupacademy.william.exception.BusinessException
import br.com.zupacademy.william.exception.PixKeyAlreadyExistsException
import br.com.zupacademy.william.exception.PixKeyNotFoundException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAdvice::class)
class ErrorInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any?>): Any? {
        return try {
            context.proceed()
        } catch (e: Exception) {
            val statusError = when (e) {
                is PixKeyNotFoundException -> buildRuntimeExceptionWithStatuProto(e)
                is AccountNotFoundException -> buildRuntimeExceptionWithStatuProto(e)
                is PixKeyAlreadyExistsException -> buildRuntimeExceptionWithStatuProto(e)
                is HttpClientResponseException -> StatusProto.toStatusRuntimeException(
                    com.google.rpc.Status.newBuilder().setCode(e.status.code).setMessage(e.message).build()
                )
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message).asRuntimeException()
                is ConstraintViolationException -> handleConstraintValidationException(e)
                else -> Status.UNKNOWN.withDescription("unexpected error happened").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
        }
    }

    private fun buildRuntimeExceptionWithStatuProto(e: BusinessException): RuntimeException {
        return StatusProto.toStatusRuntimeException(
            com.google.rpc.Status.newBuilder()
                .setCode(e.status.code.value())
                .setMessage(e.message)
                .build()
        )
    }

    private fun handleConstraintValidationException(e: ConstraintViolationException): StatusRuntimeException {
        val errors = e.constraintViolations
            .map { error ->
                ErrorDetails.newBuilder()
                    .setField(error.propertyPath.toString())
                    .setMessage(error.message)
                    .build()
            }
            .toList()

        val corpoDeErro = ErrorBody.newBuilder()
            .addAllErrors(errors)
            .build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Status.INVALID_ARGUMENT.code.value())
            .setMessage("há parametros incorretos")
            .addDetails(com.google.protobuf.Any.pack(corpoDeErro))
            .build()

        return StatusProto.toStatusRuntimeException(statusProto)
    }
}
