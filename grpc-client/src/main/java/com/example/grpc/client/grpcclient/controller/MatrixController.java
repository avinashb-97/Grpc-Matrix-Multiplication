package com.example.grpc.client.grpcclient.controller;

import com.example.grpc.client.grpcclient.service.GRPCClientService;
import com.example.grpc.client.grpcclient.utils.MatrixUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
public class MatrixController {

    GRPCClientService grpcClientService;
    @Autowired
    public MatrixController(GRPCClientService grpcClientService) {
        this.grpcClientService = grpcClientService;
    }

//    @GetMapping("/add")
//    public String add() {
//        return grpcClientService.add();
//    }

    @PostMapping("/multiply")
    public void uploadFile(@RequestParam("file") MultipartFile file, @RequestParam int deadline)
    {
        if(file == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File Not Found");
        }

        List<String> list = new ArrayList<>();
        try {
            InputStream inputStream = file.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            list = br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[][] matA = MatrixUtils.getMatrixA(list);
        int[][] matB = MatrixUtils.getMatrixB(list);

        System.out.println(deadline);
        int[][] res = new int[0][];
        try {
            res = grpcClientService.multiply(matA, matB, deadline);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MatrixUtils.printMatrix(res);
    }

}
