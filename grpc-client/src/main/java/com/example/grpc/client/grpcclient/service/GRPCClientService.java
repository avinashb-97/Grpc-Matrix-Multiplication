package com.example.grpc.client.grpcclient.service;

import com.example.grpc.client.grpcclient.GrpcClientApplication;
import com.example.grpc.client.grpcclient.utils.MatrixUtils;
import com.example.grpc.server.grpcserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service
@EnableAsync
public class GRPCClientService {

	@Autowired
	GRPCMutliUtil grpcMutliUtil;

	int stubNum = 0;

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

//	public int[][] multiply(int[][] matA, int[][] matB)
//	{
//		return  multiply(matA, matB, 8);
//	}

	public int[][] multiply(int[][] matA, int[][] matB, int deadline) throws ExecutionException, InterruptedException {

		ManagedChannel channel1 = ManagedChannelBuilder.forAddress("localhost",50000).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub1 = MatrixServiceGrpc.newBlockingStub(channel1);
		ManagedChannel channel2 = ManagedChannelBuilder.forAddress("localhost",50001).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub2 = MatrixServiceGrpc.newBlockingStub(channel2);
		ManagedChannel channel3 = ManagedChannelBuilder.forAddress("localhost",50002).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub3 = MatrixServiceGrpc.newBlockingStub(channel3);
		ManagedChannel channel4 = ManagedChannelBuilder.forAddress("localhost",50003).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub4 = MatrixServiceGrpc.newBlockingStub(channel4);
		ManagedChannel channel5 = ManagedChannelBuilder.forAddress("localhost",50004).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub5 = MatrixServiceGrpc.newBlockingStub(channel5);
		ManagedChannel channel6 = ManagedChannelBuilder.forAddress("localhost",50005).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub6 = MatrixServiceGrpc.newBlockingStub(channel6);
		ManagedChannel channel7 = ManagedChannelBuilder.forAddress("localhost",50006).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub7 = MatrixServiceGrpc.newBlockingStub(channel7);
		ManagedChannel channel8 = ManagedChannelBuilder.forAddress("localhost",50007).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub8 = MatrixServiceGrpc.newBlockingStub(channel8);

		List<ManagedChannel> channels = Arrays.asList(channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8);
		List<MatrixServiceGrpc.MatrixServiceBlockingStub> stubs = Arrays.asList(stub1, stub2, stub3, stub4, stub5, stub6, stub7, stub8);
		stubNum = 0;

		int MAX = matA.length;
		int bSize = MAX/2;

		int[][][] aBlocks = splitToBlocks(matA);
		int[][][] bBlocks = splitToBlocks(matB);

		String[] aEncodedBlocks = MatrixUtils.encodeBlocks(aBlocks);
		String[] bEncodedBlocks = MatrixUtils.encodeBlocks(bBlocks);

		long startTime = System.nanoTime();
		int[][] A, B, C, D;

		final int numServer = getNeededServerNum(aEncodedBlocks[0], bEncodedBlocks[0], stub1, deadline);

		CompletableFuture<String> AP1 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[0], bEncodedBlocks[0], getStub(stubs, numServer));
		CompletableFuture<String> AP2 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[1], bEncodedBlocks[2], getStub(stubs, numServer));
		CompletableFuture<String> BP1 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[0], bEncodedBlocks[1], getStub(stubs, numServer));
		CompletableFuture<String> BP2 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[1], bEncodedBlocks[3], getStub(stubs, numServer));
		CompletableFuture<String> CP1 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[2], bEncodedBlocks[0], getStub(stubs, numServer));
		CompletableFuture<String> CP2 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[3], bEncodedBlocks[2], getStub(stubs, numServer));
		CompletableFuture<String> DP1 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[2], bEncodedBlocks[1], getStub(stubs, numServer));
		CompletableFuture<String> DP2 = grpcMutliUtil.multiplyAsync(aEncodedBlocks[3], bEncodedBlocks[3], getStub(stubs, numServer));
		CompletableFuture.allOf(AP1, AP2, BP1, BP2, CP1, CP2, DP1, DP2);

		CompletableFuture<String> AMat = grpcMutliUtil.addAsync(AP1.get(), AP2.get(), getStub(stubs, numServer));
		CompletableFuture<String> BMat = grpcMutliUtil.addAsync(BP1.get(), BP2.get(), getStub(stubs, numServer));
		CompletableFuture<String> CMat = grpcMutliUtil.addAsync(CP1.get(), CP2.get(), getStub(stubs, numServer));
		CompletableFuture<String> DMat = grpcMutliUtil.addAsync(DP1.get(), DP2.get(), getStub(stubs, numServer));
		CompletableFuture.allOf(AMat, BMat, CMat, DMat);

		A = MatrixUtils.decodeMatrix(AMat.get());
		B = MatrixUtils.decodeMatrix(BMat.get());
		C = MatrixUtils.decodeMatrix(CMat.get());
		D = MatrixUtils.decodeMatrix(DMat.get());

		int[][] res = calResult(A, B, C, D, MAX, bSize);
		long stopTime = System.nanoTime();
		System.out.println(stopTime - startTime);
		shutdownChannels(channels);
		return res;
	}

	private int getNeededServerNum(String matA, String matB, MatrixServiceGrpc.MatrixServiceBlockingStub stub, int deadline) throws ExecutionException, InterruptedException {
		long startTIme = System.currentTimeMillis();
		stub.multiplyBlock(MatrixRequest.newBuilder().setA(matA).setB(matB).build()).getC();
		long endTime = System.currentTimeMillis();
		long footprint= endTime-startTIme;
		System.out.println("Footprint : "+footprint);
		int numberServer= (int)(footprint*12)/deadline;
		System.out.println("numberServer : "+numberServer);
		int needed;
		if(numberServer == 0)
		{
			needed = 1;
		}
		else
		{
			needed = numberServer < 8 ? numberServer : 8;
		}
		return needed;
	}


	private MatrixServiceGrpc.MatrixServiceBlockingStub getStub(List<MatrixServiceGrpc.MatrixServiceBlockingStub> stubs, int deadline)
	{
		if(stubNum == deadline)
		{
			stubNum = 0;
		}
		return stubs.get(stubNum++);
	}

	private void shutdownChannels(List<ManagedChannel> channels)
	{
		for (ManagedChannel channel : channels)
		{
			channel.shutdown();
		}
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
