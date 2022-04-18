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
	}

}
