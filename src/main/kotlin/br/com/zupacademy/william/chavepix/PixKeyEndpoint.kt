package br.com.zupacademy.william.chavepix

import br.com.zupacademy.william.*
import br.com.zupacademy.william.exception.AccountNotFoundException
import br.com.zupacademy.william.exception.PixKeyAlreadyExistsException
import com.google.protobuf.Any
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class PixKeyEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceImplBase() {

    override fun registry(
        request: KeymanagerGrpcRequest?,
        responseObserver: StreamObserver<KeymanagerGrpcReply>?
    ) {
        try {
            val novaChavePix = request!!.toModel()
            val response = pixKeyService.save(novaChavePix)

            val keymanagerGrpcReply = KeymanagerGrpcReply.newBuilder()
                .setIdPix(response.id.toString())
                .build()

            responseObserver!!.onNext(keymanagerGrpcReply)
            responseObserver.onCompleted()

        } catch (exception: PixKeyAlreadyExistsException) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(exception.status.code.value())
                .setMessage(exception.message)
                .build()

            val exceptionResposta = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver!!.onError(exceptionResposta)

        } catch (exception: HttpClientResponseException) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(exception.status.code)
                .setMessage(exception.message)
                .build()

            val exceptionResposta = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver!!.onError(exceptionResposta)

        } catch (exception: ConstraintViolationException) {
            val errors = exception.constraintViolations
                .map { error ->
                    ErrorDetails.newBuilder()
                        .setCampo(error.propertyPath.toString())
                        .setErro(error.message)
                        .build()
                }
                .toList()

            val corpoDeErro = CorpoDeErro.newBuilder()
                .addAllErrors(errors)
                .build()

            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Status.INVALID_ARGUMENT.code.value())
                .setMessage("há parametros incorretos")
                .addDetails(Any.pack(corpoDeErro))
                .build()

            val exceptionResposta = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver!!.onError(exceptionResposta)

        } catch (exception: IllegalArgumentException) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Status.INVALID_ARGUMENT.code.value())
                .setMessage(exception.message)
                .build()

            val exceptionResposta = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver!!.onError(exceptionResposta)
        } catch (exception: AccountNotFoundException) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(exception.status.code.value())
                .setMessage(exception.message)
                .build()

            val exceptionResposta = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver!!.onError(exceptionResposta)
        }
    }
}