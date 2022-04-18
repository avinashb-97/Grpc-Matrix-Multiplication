package com.example.grpc.client.grpcclient.service;

import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class GRPCMutliUtil{

    @Async("asyncExecutor")
    public CompletableFuture<String> multiplyAsync(String matA, String matB, MatrixServiceGrpc.MatrixServiceBlockingStub stub)
    {
        System.out.println("Multiply started");
        return CompletableFuture.completedFuture(stub.multiplyBlock(MatrixRequest.newBuilder().setA(matA).setB(matB).build()).getC());
    }

    @Async("asyncExecutor")
    public CompletableFuture<String> addAsync(String matA, String matB, MatrixServiceGrpc.MatrixServiceBlockingStub stub)
    {
        return CompletableFuture.completedFuture(stub.addBlock(MatrixRequest.newBuilder().setA(matA).setB(matB).build()).getC());
    }


}
