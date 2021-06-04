package br.com.zupacademy.william.pixkey.find

import br.com.zupacademy.william.FindRequest
import br.com.zupacademy.william.FindResponse
import br.com.zupacademy.william.KeymanagerFindGrpcServiceGrpc
import br.com.zupacademy.william.exception.PixKeyNotFoundException
import br.com.zupacademy.william.pixkey.PixKeyService
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PixKeyFindEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerFindGrpcServiceGrpc.KeymanagerFindGrpcServiceImplBase() {


    override fun find(request: FindRequest?, responseObserver: StreamObserver<FindResponse>?) {
        try {
            val response = pixKeyService.find(request!!.idPix, request.idCustomer)

            responseObserver!!.onNext(response)
            responseObserver.onCompleted()
        } catch (exception: PixKeyNotFoundException) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(exception.status.code.value())
                .setMessage(exception.message)
                .build()

            val exceptionResponse = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver!!.onError(exceptionResponse)
        }
    }
}