package br.com.zupacademy.william.pixkey.remove

import br.com.zupacademy.william.KeymanagerRemoveGrpcServiceGrpc
import br.com.zupacademy.william.RemoveRequest
import br.com.zupacademy.william.RemoveResponse
import br.com.zupacademy.william.exception.PixKeyNotFoundException
import br.com.zupacademy.william.pixkey.PixKeyService
import com.google.rpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PixKeyRemoveEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceImplBase() {

    override fun remove(request: RemoveRequest?, responseObserver: StreamObserver<RemoveResponse>?) {
        try {
            pixKeyService.delete(request!!.idCustomer, request.idPix)

            val response = RemoveResponse.newBuilder()
                .setIdCustomer(request.idCustomer)
                .setIdPix(request.idPix)
                .build()

            responseObserver!!.onNext(response)
            responseObserver.onCompleted()
        } catch (exception: PixKeyNotFoundException) {
            val statusProto = Status.newBuilder()
                .setCode(exception.status.code.value())
                .setMessage(exception.message)
                .build()

            val responseException = StatusProto.toStatusRuntimeException(statusProto)

            return responseObserver!!.onError(responseException)
        }
    }
}