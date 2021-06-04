package br.com.zupacademy.william.pixkey.list

import br.com.zupacademy.william.KeymanagerListGrpcServiceGrpc
import br.com.zupacademy.william.ListRequest
import br.com.zupacademy.william.ListResponse
import br.com.zupacademy.william.exception.PixKeyNotFoundException
import br.com.zupacademy.william.pixkey.PixKeyService
import com.google.rpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PixKeyListEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerListGrpcServiceGrpc.KeymanagerListGrpcServiceImplBase() {

    override fun list(request: ListRequest?, responseObserver: StreamObserver<ListResponse>?) {
        try {
            val response = pixKeyService.list(request!!.idCustomer)

            responseObserver!!.onNext(response)
            responseObserver.onCompleted()
        } catch (exception: PixKeyNotFoundException) {
            val statusProto = Status.newBuilder()
                .setCode(exception.status.code.value())
                .setMessage(exception.message)
                .build()

            val exceltionResponse = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver!!.onError(exceltionResponse)
        }
    }
}