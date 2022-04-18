package com.example.grpc.client.grpcclient.service;

import com.example.grpc.client.grpcclient.utils.MatrixUtils;
import com.example.grpc.server.grpcserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@EnableAsync
public class GRPCClientService {

	@Autowired
	GRPCAsyncCalcService grpcAsyncCalcService;

	int stubNum = 0;
	int numServer = 0;
	int totalServers = 0;

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

	//Performs matrix block multiplication using n number of servers based on given deadline
	public int[][] multiply(int[][] matA, int[][] matB, int deadline) throws ExecutionException, InterruptedException {

		// Create channels and respective stubs to establish connection to GRPC servers
		ManagedChannel channel1 = ManagedChannelBuilder.forAddress("35.238.96.183",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub1 = MatrixServiceGrpc.newBlockingStub(channel1);
		ManagedChannel channel2 = ManagedChannelBuilder.forAddress("35.184.111.129",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub2 = MatrixServiceGrpc.newBlockingStub(channel2);
		ManagedChannel channel3 = ManagedChannelBuilder.forAddress("35.223.152.1",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub3 = MatrixServiceGrpc.newBlockingStub(channel3);
		ManagedChannel channel4 = ManagedChannelBuilder.forAddress("34.66.143.88",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub4 = MatrixServiceGrpc.newBlockingStub(channel4);
		ManagedChannel channel5 = ManagedChannelBuilder.forAddress("35.184.54.220",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub5 = MatrixServiceGrpc.newBlockingStub(channel5);
		ManagedChannel channel6 = ManagedChannelBuilder.forAddress("34.132.132.87",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub6 = MatrixServiceGrpc.newBlockingStub(channel6);
		ManagedChannel channel7 = ManagedChannelBuilder.forAddress("34.71.156.76",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub7 = MatrixServiceGrpc.newBlockingStub(channel7);
		ManagedChannel channel8 = ManagedChannelBuilder.forAddress("35.234.156.7",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub8 = MatrixServiceGrpc.newBlockingStub(channel8);
		ManagedChannel channel9 = ManagedChannelBuilder.forAddress("34.134.39.131",8082).usePlaintext().build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub9 = MatrixServiceGrpc.newBlockingStub(channel8);

		List<ManagedChannel> channels = Arrays.asList(channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8, channel9);
		List<MatrixServiceGrpc.MatrixServiceBlockingStub> stubs = Arrays.asList(stub1, stub2, stub3, stub4, stub5, stub6, stub7, stub8, stub9);
		totalServers = stubs.size();

		//Split the matrices into blocks to perform matrix block multiplication
		int[][][] aBlocks = MatrixUtils.splitToBlocks(matA);
		int[][][] bBlocks = MatrixUtils.splitToBlocks(matB);

		//Encode the blocks into Strings to pass into server for matrix addition and multiplication
		String[] aEncodedBlocks = MatrixUtils.encodeBlocks(aBlocks);
		String[] bEncodedBlocks = MatrixUtils.encodeBlocks(bBlocks);
		int[][] A, B, C, D;

		//Find number of servers needed to perform operation based on given deadline
		final int numServer = getNeededServerNum(aEncodedBlocks[0], bEncodedBlocks[0], stub1, deadline);
		initializeStubAndMaxServers(numServer);
		System.out.println("Total number of servers needed : "+numServer);

		//Note the start time to find time taken for matrix multiplication
		long startTime = System.currentTimeMillis();

		//Performing block multiplication asynchronously using needed number of servers
		CompletableFuture<String> AP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[0], bEncodedBlocks[0], getStub(stubs));
		CompletableFuture<String> AP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[1], bEncodedBlocks[2], getStub(stubs));
		CompletableFuture<String> BP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[0], bEncodedBlocks[1], getStub(stubs));
		CompletableFuture<String> BP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[1], bEncodedBlocks[3], getStub(stubs));
		CompletableFuture<String> CP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[2], bEncodedBlocks[0], getStub(stubs));
		CompletableFuture<String> CP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[3], bEncodedBlocks[2], getStub(stubs));
		CompletableFuture<String> DP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[2], bEncodedBlocks[1], getStub(stubs));
		CompletableFuture<String> DP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[3], bEncodedBlocks[3], getStub(stubs));
		CompletableFuture<String> AMat = grpcAsyncCalcService.addAsync(AP1.get(), AP2.get(), getStub(stubs));
		CompletableFuture<String> BMat = grpcAsyncCalcService.addAsync(BP1.get(), BP2.get(), getStub(stubs));
		CompletableFuture<String> CMat = grpcAsyncCalcService.addAsync(CP1.get(), CP2.get(), getStub(stubs));
		CompletableFuture<String> DMat = grpcAsyncCalcService.addAsync(DP1.get(), DP2.get(), getStub(stubs));

		//Decoding the matrix string back to 2D matrix
		A = MatrixUtils.decodeMatrix(AMat.get());
		B = MatrixUtils.decodeMatrix(BMat.get());
		C = MatrixUtils.decodeMatrix(CMat.get());
		D = MatrixUtils.decodeMatrix(DMat.get());

		//Combining the matrix block back into a single matrix
		int[][] res = MatrixUtils.calResult(A, B, C, D, matA.length, matA.length/2);

		//Noting the stop time to calculate the total time taken for matrix calculation
		long stopTime = System.currentTimeMillis();
		System.out.println("Total time taken for matrix multiplication : "+(stopTime - startTime)+" ms");

		//Shutdown all created channels
		shutdownChannels(channels);
		return res;
	}

	//Initializes the number of servers needed and initial stub number
	private void initializeStubAndMaxServers(int numServer)
	{
		this.stubNum = 0;
		this.numServer = numServer;
	}

	//Returns the number of server needed for performing matrix multiplication based on given deadline
	private int getNeededServerNum(String matA, String matB, MatrixServiceGrpc.MatrixServiceBlockingStub stub, int deadline) throws ExecutionException, InterruptedException {

		//Calculate time needed for single block matrix multiplication
		long startTIme = System.currentTimeMillis();
		String res = stub.multiplyBlock(MatrixRequest.newBuilder().setA(matA).setB(matB).build()).getC();
		long endTime = System.currentTimeMillis();
		long footprint = endTime - startTIme;
		System.out.println("Footprint : " + footprint);

		//Calculating the servers needed for entire matrix multiplication based on single block multiplication
		int numberServer = (int) (footprint * 12) / deadline;
		if (numberServer < 1) {
			return 1;
		}
		return numberServer < this.totalServers ? numberServer : this.totalServers;
	}

	//Returns the stub needed for operation
	private MatrixServiceGrpc.MatrixServiceBlockingStub getStub(List<MatrixServiceGrpc.MatrixServiceBlockingStub> stubs)
	{
		if(stubNum == numServer)
		{
			stubNum = 0;
		}
		return stubs.get(stubNum++);
	}

	//Shutdowns all the running channels iteratively
	private void shutdownChannels(List<ManagedChannel> channels)
	{
		for (ManagedChannel channel : channels)
		{
			channel.shutdown();
		}
	}
}
