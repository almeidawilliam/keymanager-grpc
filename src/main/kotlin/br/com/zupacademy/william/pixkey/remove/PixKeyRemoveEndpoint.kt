package br.com.zupacademy.william.pixkey.remove

import br.com.zupacademy.william.KeymanagerRemoveServiceGrpc
import br.com.zupacademy.william.RemoveRequest
import br.com.zupacademy.william.RemoveResponse
import br.com.zupacademy.william.pixkey.PixKeyService
import br.com.zupacademy.william.validation.interceptor.ErrorAdvice
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAdvice
@Singleton
class PixKeyRemoveEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceImplBase() {

    override fun remove(request: RemoveRequest?, responseObserver: StreamObserver<RemoveResponse>?) {
        pixKeyService.delete(request!!.idCustomer, request.idPix)

        val response = RemoveResponse.newBuilder()
            .setIdCustomer(request.idCustomer)
            .setIdPix(request.idPix)
            .build()

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()

    }
}