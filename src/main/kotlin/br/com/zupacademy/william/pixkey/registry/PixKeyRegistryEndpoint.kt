package br.com.zupacademy.william.pixkey.registry

import br.com.zupacademy.william.KeymanagerRegistryServiceGrpc
import br.com.zupacademy.william.RegistryRequest
import br.com.zupacademy.william.RegistryResponse
import br.com.zupacademy.william.pixkey.PixKeyService
import br.com.zupacademy.william.validation.interceptor.ErrorAdvice
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAdvice
@Singleton
class PixKeyRegistryEndpoint(@field:Inject val pixKeyService: PixKeyService) :
    KeymanagerRegistryServiceGrpc.KeymanagerRegistryServiceImplBase() {

    override fun registry(
        request: RegistryRequest?,
        responseObserver: StreamObserver<RegistryResponse>?
    ) {
        val novaChavePix = request!!.toModel()
        val response = pixKeyService.save(novaChavePix)

        val keymanagerGrpcReply = RegistryResponse.newBuilder()
            .setIdPix(response.id.toString())
            .build()

        responseObserver!!.onNext(keymanagerGrpcReply)
        responseObserver.onCompleted()
    }
}
