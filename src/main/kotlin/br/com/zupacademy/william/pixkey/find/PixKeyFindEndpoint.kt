package br.com.zupacademy.william.pixkey.find

import br.com.zupacademy.william.FindRequest
import br.com.zupacademy.william.FindResponse
import br.com.zupacademy.william.KeymanagerFindGrpcServiceGrpc
import br.com.zupacademy.william.pixkey.PixKeyService
import br.com.zupacademy.william.validation.interceptor.ErrorAdvice
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAdvice
@Singleton
class PixKeyFindEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerFindGrpcServiceGrpc.KeymanagerFindGrpcServiceImplBase() {

    override fun find(request: FindRequest?, responseObserver: StreamObserver<FindResponse>?) {
        val response = pixKeyService.find(request!!.idPix, request.idCustomer)

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}