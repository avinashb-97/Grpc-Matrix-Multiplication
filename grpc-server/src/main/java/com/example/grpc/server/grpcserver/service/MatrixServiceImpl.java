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
	/**
	 *	Performs addition of two matrix blocks
	 */
	@Override
	public void addBlock(MatrixRequest request, StreamObserver<MatrixReply> reply)
	{

		//Decode the client request string to 2D matrix
		int[][] matA = MatrixUtils.decodeMatrix(request.getA());
		int[][] matB = MatrixUtils.decodeMatrix(request.getB());
		System.out.println("Matrix addition request received from client:\n" + request);

		//Create 2D matrix to store result
		int row = matA.length;
		int col = matA[0].length;
		int[][] matC = new int[row][col];

		//Perform matrix addition for 2 matrices
		for(int i=0; i<row; i++)
		{
			for(int j=0; j<col; j++)
			{
				matC[i][j] = matA[i][j] + matB[i][j];
			}
		}

		//Encode the 2D matrix to string format
		String res = MatrixUtils.encodeMatrix(matC);

		//Return the encoded 2D matrix to client
		MatrixReply response = MatrixReply.newBuilder().setC(res).build();
		reply.onNext(response);
		reply.onCompleted();
	}

	/**
	 * Performs multiplication of two matrix blocks
	 */
	@Override
	public void multiplyBlock(MatrixRequest request, StreamObserver<MatrixReply> reply)
	{
		//Decode the client request string to 2D matrix
		int[][] matA = MatrixUtils.decodeMatrix(request.getA());
		int[][] matB = MatrixUtils.decodeMatrix(request.getB());
		System.out.println("Matrix multiplication request received from client:\n" + request);

		//Create 2D matrix to store result
		int row = matA.length;
		int col = matA[0].length;
		int[][] matC = new int[row][col];

		//Perform matrix multiplication for 2 matrices
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

		//Encode the 2D matrix to string format
		String res = MatrixUtils.encodeMatrix(matC);

		//Return the encoded 2D matrix to client
        MatrixReply response = MatrixReply.newBuilder().setC(res).build();
        reply.onNext(response);
        reply.onCompleted();
    }
}
