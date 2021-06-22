package br.com.zupacademy.william.pixkey.list

import br.com.zupacademy.william.KeymanagerListGrpcServiceGrpc
import br.com.zupacademy.william.ListRequest
import br.com.zupacademy.william.ListResponse
import br.com.zupacademy.william.pixkey.PixKeyService
import br.com.zupacademy.william.validation.interceptor.ErrorAdvice
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAdvice
@Singleton
class PixKeyListEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerListGrpcServiceGrpc.KeymanagerListGrpcServiceImplBase() {

    override fun list(request: ListRequest?, responseObserver: StreamObserver<ListResponse>?) {
        val response = pixKeyService.list(request!!.idCustomer)

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}