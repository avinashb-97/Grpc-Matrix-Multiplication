package com.example.grpc.server.grpcserver.service;


import com.example.grpc.server.grpcserver.MatrixReply;
import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import com.example.grpc.server.grpcserver.utils.MatrixUtils;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class MatrixServiceImpl extends MatrixServiceGrpc.MatrixServiceImplBase
{
	private String name;

	public MatrixServiceImpl(){

	}

	public MatrixServiceImpl(String name) {
		this.name = name;
	}

	@Override
	public void addBlock(MatrixRequest request, StreamObserver<MatrixReply> reply)
	{
		int[][] matA = MatrixUtils.decodeMatrix(request.getA());
		int[][] matB = MatrixUtils.decodeMatrix(request.getB());
		int row = matA.length;
		int col = matA[0].length;

		System.out.println("Matrix addition request received from client:\n" + request+" , Server: "+name);
		int[][] matC = new int[row][col];

		for(int i=0; i<row; i++)
		{
			for(int j=0; j<col; j++)
			{
				matC[i][j] = matA[i][j] + matB[i][j];
			}
		}

		String res = MatrixUtils.encodeMatrix(matC);
		MatrixReply response = MatrixReply.newBuilder().setC(res).build();
		reply.onNext(response);
		reply.onCompleted();
	}

	@Override
	public void multiplyBlock(MatrixRequest request, StreamObserver<MatrixReply> reply)
	{
		int[][] matA = MatrixUtils.decodeMatrix(request.getA());
		int[][] matB = MatrixUtils.decodeMatrix(request.getB());
		int row = matA.length;
		int col = matA[0].length;

		System.out.println("Matrix multiplication request received from client:\n" + request+" , Server: "+name);
		int[][] matC = new int[row][col];

		for(int i=0; i<row; i++)
		{
			for(int j=0; j<col; j++)
			{
				matC[i][j] = 0;
				for(int k=0; k<row; k++)
				{
					matC[i][j] += matA[i][k] * matB[k][j];
				}
			}
		}

		String res = MatrixUtils.encodeMatrix(matC);
        MatrixReply response = MatrixReply.newBuilder().setC(res).build();
        reply.onNext(response);
        reply.onCompleted();
    }
}
