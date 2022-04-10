package com.example.grpc.server.grpcserver;

import com.example.grpc.server.grpcserver.service.MatrixServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class GrpcServerApplication extends SpringBootServletInitializer {

	public static void main(String[] args)
	{
		SpringApplication.run(GrpcServerApplication.class, args);
		int nServers = 8;
		ExecutorService executorService = Executors.newFixedThreadPool(nServers);
		for (int i = 0; i < nServers; i++) {
			int port = 50000 + i;
			executorService.submit(() -> {
				try {
					startServer(port);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private static void startServer(int port) throws IOException, InterruptedException {
		Server server = ServerBuilder
				.forPort(port)
				.addService(new MatrixServiceImpl(port+""))
				.build();

		server.start();
		System.out.println(" server started, listening on port: " + server.getPort());
		server.awaitTermination();
	}

}
