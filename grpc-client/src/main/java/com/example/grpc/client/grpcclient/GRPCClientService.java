package com.example.grpc.client.grpcclient;

import com.example.grpc.server.grpcserver.PingRequest;
import com.example.grpc.server.grpcserver.PongResponse;
import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixReply;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;

@Service
public class GRPCClientService {

	static int MAX = 0;

    public String ping() {
        	ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();        
		PingPongServiceGrpc.PingPongServiceBlockingStub stub
                = PingPongServiceGrpc.newBlockingStub(channel);        
		PongResponse helloResponse = stub.ping(PingRequest.newBuilder()
                .setPing("")
                .build());        
		channel.shutdown();        
		return helloResponse.getPong();
    }

	public void add(int[][] matA, int[][] matB){
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
				.usePlaintext()
				.build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub
				= MatrixServiceGrpc.newBlockingStub(channel);

		String matrixA = MatrixUtils.encodeMatrix(matA);
		String matrixB = MatrixUtils.encodeMatrix(matB);

		MatrixReply res=stub.addBlock(MatrixRequest.newBuilder()
				.setA(matrixA)
				.setB(matrixB)
				.build());
		String resp= res.getC();
		int[][] C = MatrixUtils.decodeMatrix(resp);
		MatrixUtils.printMatrix(C);
	}

	public int[][] multiply(int[][] matA, int[][] matB){

		NameResolver.Factory nameResolverFactory = new MultiAddressNameResolverFactory(
				new InetSocketAddress("localhost", 50000),
				new InetSocketAddress("localhost", 50001),
				new InetSocketAddress("localhost", 50002),
				new InetSocketAddress("localhost", 50003),
				new InetSocketAddress("localhost", 50004),
				new InetSocketAddress("localhost", 50005),
				new InetSocketAddress("localhost", 50006),
				new InetSocketAddress("localhost", 50007)
		);

		ManagedChannel channel = ManagedChannelBuilder.forTarget("service")
				.nameResolverFactory(nameResolverFactory)
				.defaultLoadBalancingPolicy("round_robin")
				.usePlaintext()
				.build();

		MatrixServiceGrpc.MatrixServiceBlockingStub stub = MatrixServiceGrpc.newBlockingStub(channel);

		int MAX = matA.length;
		int bSize = MAX/2;

		int[][][] aBlocks = splitToBlocks(matA);
		int[][][] bBlocks = splitToBlocks(matB);

		String[] aEncodedBlocks = MatrixUtils.encodeBlocks(aBlocks);
		String[] bEncodedBlocks = MatrixUtils.encodeBlocks(bBlocks);

		long startTime = System.nanoTime();

		int[][] A, B, C, D;
		String AP1 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[0]).setB(bEncodedBlocks[0]).build()).getC();
		String AP2 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[1]).setB(bEncodedBlocks[2]).build()).getC();
		A = MatrixUtils.decodeMatrix(stub.addBlock(MatrixRequest.newBuilder().setA(AP1).setB(AP2).build()).getC());

		String BP1 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[0]).setB(bEncodedBlocks[1]).build()).getC();
		String BP2 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[1]).setB(bEncodedBlocks[3]).build()).getC();
		B = MatrixUtils.decodeMatrix(stub.addBlock(MatrixRequest.newBuilder().setA(BP1).setB(BP2).build()).getC());

		String CP1 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[2]).setB(bEncodedBlocks[0]).build()).getC();
		String CP2 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[3]).setB(bEncodedBlocks[2]).build()).getC();
		C = MatrixUtils.decodeMatrix(stub.addBlock(MatrixRequest.newBuilder().setA(BP1).setB(BP2).build()).getC());

		String DP1 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[2]).setB(bEncodedBlocks[1]).build()).getC();
		String DP2 = stub.multiplyBlock(MatrixRequest.newBuilder().setA(aEncodedBlocks[3]).setB(bEncodedBlocks[3]).build()).getC();
		D = MatrixUtils.decodeMatrix(stub.addBlock(MatrixRequest.newBuilder().setA(BP1).setB(BP2).build()).getC());

		int[][] res = calResult(A, B, C, D, MAX, bSize);

		long stopTime = System.nanoTime();
		System.out.println(stopTime - startTime);

		return res;
	}

	static int[][] multiplyMatrixBlock( int matA[][], int matB[][])
	{
		int MAX = matA.length;
		int bSize = MAX/2;

		int[][][] aBlocks = splitToBlocks(matA);
		int[][][] bBlocks = splitToBlocks(matB);
		int[][] A, B, C, D;
		A = addBlock(multiplyBlock(aBlocks[0], bBlocks[0]),multiplyBlock(aBlocks[1], bBlocks[2]));
		B = addBlock(multiplyBlock(aBlocks[0], bBlocks[1]),multiplyBlock(aBlocks[1], bBlocks[3]));
		C = addBlock(multiplyBlock(aBlocks[2], bBlocks[0]),multiplyBlock(aBlocks[3], bBlocks[2]));
		D = addBlock(multiplyBlock(aBlocks[2], bBlocks[1]),multiplyBlock(aBlocks[3], bBlocks[3]));
		int[][] res = calResult(A, B, C, D, MAX, bSize);
		return res;
	}

	public static int[][][] splitToBlocks(int mat[][])
	{
		int MAX = mat.length;
		int bSize = MAX/2;

		int[][] A = new int[bSize][bSize];
		int[][] B = new int[bSize][bSize];
		int[][] C = new int[bSize][bSize];
		int[][] D = new int[bSize][bSize];
		int[][][] blocks = new int[4][bSize][bSize];

		for (int i = 0; i < bSize; i++)
		{
			for (int j = 0; j < bSize; j++)
			{
				A[i][j] = mat[i][j];
			}
		}
		for (int i = 0; i < bSize; i++)
		{
			for (int j = bSize; j < MAX; j++)
			{
				B[i][j-bSize] = mat[i][j];
			}
		}
		for (int i = bSize; i < MAX; i++)
		{
			for (int j = 0; j < bSize; j++)
			{
				C[i-bSize][j] = mat[i][j];
			}
		}
		for (int i = bSize; i < MAX; i++)
		{
			for (int j = bSize; j < MAX; j++)
			{
				D[i-bSize][j-bSize] = mat[i][j];
			}
		}
		blocks[0] = A;
		blocks[1] = B;
		blocks[2] = C;
		blocks[3] = D;
		return blocks;
	}

	public static int[][] calResult(int[][] A, int[][] B, int[][] C, int[][] D, int MAX, int bSize){

		int[][] res= new int[MAX][MAX];

		for (int i = 0; i < bSize; i++){
			for (int j = 0; j < bSize; j++){
				res[i][j]=A[i][j];
			}
		}

		for (int i = 0; i < bSize; i++){
			for (int j = bSize; j < MAX; j++){
				res[i][j]=B[i][j-bSize];
			}
		}

		for (int i = bSize; i < MAX; i++){
			for (int j = 0; j < bSize; j++){
				res[i][j]=C[i-bSize][j];
			}
		}

		for (int i = bSize; i < MAX; i++){
			for (int j = bSize; j < MAX; j++){
				res[i][j]=D[i-bSize][j-bSize];
			}
		}
		return res;
	}

	static int[][] multiplyBlock(int A[][], int B[][])
	{
		int bSize = A.length;
		int C[][]= new int[bSize][bSize];

		for (int i=0;i<bSize;i++)
		{
			for (int j=0;j<bSize;j++)
			{
				C[i][j] = 0;
				for(int k=0; k<bSize; k++)
				{
					C[i][j]+=A[i][k]*B[k][j];
				}
			}
		}
		return C;
	}

	static int[][] addBlock(int A[][], int B[][])
	{
		int bSize = A.length;

		int C[][]= new int[bSize][bSize];
		for (int i=0;i<bSize;i++)
		{
			for (int j=0;j<bSize;j++)
			{
				C[i][j]=A[i][j]+B[i][j];
			}
		}
		return C;
	}
}
