package com.example.grpc.client.grpcclient.service;

import com.example.grpc.client.grpcclient.utils.MatrixUtils;
import com.example.grpc.server.grpcserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
	List<ManagedChannel> channels;
	List<MatrixServiceGrpc.MatrixServiceBlockingStub> stubs;

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

	/**
	 * Performs matrix block multiplication using n number of servers based on given deadline
	 */
	public int[][] multiply(int[][] matA, int[][] matB, int deadline) throws ExecutionException, InterruptedException {

		//Array of GRPC servers ip address
		String[] grpcServerAddresses = new String[]{"35.238.96.183","35.184.111.129","35.223.152.1","34.66.143.88",
				"35.184.54.220","34.132.132.87","34.71.156.76","35.234.156.7","34.105.161.153","34.142.76.41"};

		// Create channels and respective stubs to establish connection to given GRPC servers
		createChannels(grpcServerAddresses, 8082);

		//Split the matrices into blocks to perform matrix block multiplication
		int[][][] aBlocks = MatrixUtils.splitToBlocks(matA);
		int[][][] bBlocks = MatrixUtils.splitToBlocks(matB);

		//Encode the blocks into Strings to pass into server for matrix addition and multiplication
		String[] aEncodedBlocks = MatrixUtils.encodeBlocks(aBlocks);
		String[] bEncodedBlocks = MatrixUtils.encodeBlocks(bBlocks);

		//Find number of servers needed to perform operation based on given deadline
		final int numServer = getNeededServerNum(aEncodedBlocks[0], bEncodedBlocks[0], stubs.get(0), deadline);
		initializeStubAndMaxServers(numServer);
		System.out.println("Total number of servers needed : "+numServer);

		//Note the start time to find time taken for matrix multiplication
		long startTime = System.currentTimeMillis();

		//Performing block multiplication asynchronously using needed number of servers
		System.out.println("Starting matrix block multiplication...");
		CompletableFuture<String> AP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[0], bEncodedBlocks[0], getStub());
		CompletableFuture<String> AP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[1], bEncodedBlocks[2], getStub());
		CompletableFuture<String> BP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[0], bEncodedBlocks[1], getStub());
		CompletableFuture<String> BP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[1], bEncodedBlocks[3], getStub());
		CompletableFuture<String> CP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[2], bEncodedBlocks[0], getStub());
		CompletableFuture<String> CP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[3], bEncodedBlocks[2], getStub());
		CompletableFuture<String> DP1 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[2], bEncodedBlocks[1], getStub());
		CompletableFuture<String> DP2 = grpcAsyncCalcService.multiplyAsync(aEncodedBlocks[3], bEncodedBlocks[3], getStub());
		CompletableFuture<String> AMat = grpcAsyncCalcService.addAsync(AP1.get(), AP2.get(), getStub());
		CompletableFuture<String> BMat = grpcAsyncCalcService.addAsync(BP1.get(), BP2.get(), getStub());
		CompletableFuture<String> CMat = grpcAsyncCalcService.addAsync(CP1.get(), CP2.get(), getStub());
		CompletableFuture<String> DMat = grpcAsyncCalcService.addAsync(DP1.get(), DP2.get(), getStub());

		//Decoding the matrix string back to 2D matrix
		int[][] A, B, C, D;
		A = MatrixUtils.decodeMatrix(AMat.get());
		B = MatrixUtils.decodeMatrix(BMat.get());
		C = MatrixUtils.decodeMatrix(CMat.get());
		D = MatrixUtils.decodeMatrix(DMat.get());

		//Combining the matrix block back into a single matrix
		int[][] res = MatrixUtils.calResult(A, B, C, D, matA.length, matA.length/2);

		//Noting the stop time to calculate the total time taken for matrix calculation
		long stopTime = System.currentTimeMillis();
		System.out.println("Matrix multiplication completed.");
		System.out.println("Total time taken for matrix multiplication : "+(stopTime - startTime)+" ms \n");

		//Shutdown all created channels
		shutdownChannels();
		return res;
	}

	/**
	 * Creates channels and stubs for given array of ip addresses to establish connection with GRPC servers
	 */
	private void createChannels(String[] ipAddresses, int port)
	{
		channels = new ArrayList<>();
		stubs = new ArrayList<>();
		for(String address : ipAddresses)
		{
			ManagedChannel channel = ManagedChannelBuilder.forAddress(address,8082).usePlaintext().build();
			MatrixServiceGrpc.MatrixServiceBlockingStub stub = MatrixServiceGrpc.newBlockingStub(channel);
			channels.add(channel);
			stubs.add(stub);
		}
		totalServers = stubs.size();
	}

	/**
	 * Initializes the number of servers needed and initial stub number
	 */
	private void initializeStubAndMaxServers(int numServer)
	{
		this.stubNum = 0;
		this.numServer = numServer;
	}

	/**
	 * Returns the number of server needed for performing matrix multiplication based on given deadline
	 */
	private int getNeededServerNum(String matA, String matB, MatrixServiceGrpc.MatrixServiceBlockingStub stub, int deadline) throws ExecutionException, InterruptedException {

		//Calculate time needed for single block matrix multiplication
		long startTIme = System.currentTimeMillis();
		String res = stub.multiplyBlock(MatrixRequest.newBuilder().setA(matA).setB(matB).build()).getC();
		long endTime = System.currentTimeMillis();
		long footprint = endTime - startTIme;
		System.out.println("Footprint : " + footprint+" ms");

		//Calculating the servers needed for entire matrix multiplication based on single block multiplication
		int numberServer = (int) (footprint * 12) / deadline;
		if (numberServer < 1) {
			return 1;
		}
		return numberServer < this.totalServers ? numberServer : this.totalServers;
	}

	/**
	 * Returns the stub needed for operation
	 */
	private MatrixServiceGrpc.MatrixServiceBlockingStub getStub()
	{
		if(stubNum == numServer)
		{
			stubNum = 0;
		}
		return stubs.get(stubNum++);
	}

	/**
	 * Shutdowns all the running channels iteratively
	 */
	private void shutdownChannels()
	{
		for (ManagedChannel channel : channels)
		{
			channel.shutdown();
		}
	}
}
