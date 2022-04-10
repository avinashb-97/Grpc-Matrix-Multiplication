package com.example.grpc.client.grpcclient.controller;

import com.example.grpc.client.grpcclient.service.GRPCClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class PingPongEndpoint {    

	GRPCClientService grpcClientService;
	@Autowired
    	public PingPongEndpoint(GRPCClientService grpcClientService) {
        	this.grpcClientService = grpcClientService;
    	}    
	@GetMapping("/ping")
    	public String ping() {
        	return grpcClientService.ping();
    	}
}
