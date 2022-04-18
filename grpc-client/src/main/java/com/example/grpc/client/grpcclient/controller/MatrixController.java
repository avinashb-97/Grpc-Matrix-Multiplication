package com.example.grpc.client.grpcclient.controller;

import com.example.grpc.client.grpcclient.service.GRPCClientService;
import com.example.grpc.client.grpcclient.utils.MatrixUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MatrixController {

    GRPCClientService grpcClientService;
    @Autowired
    public MatrixController(GRPCClientService grpcClientService) {
        this.grpcClientService = grpcClientService;
    }

    @PostMapping("/multiply")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam int deadline)
    {
        //Throws exception if file is empty
        if(file == null)
        {
            throw new RuntimeException("File is Empty");
        }

        //Checks and converts the given matrix file into 2D matrices
        int[][][] matrices = MatrixUtils.getMatrixFromFile(file);
        int[][] matA = matrices[0];
        int[][] matB = matrices[1];

        //Perform block matrix multiplication
        int[][] res;
        try {
            res = grpcClientService.multiply(matA, matB, deadline);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while performing block matrix multiplication "+e);
        }
        return MatrixUtils.encodeMatrix(res);
    }

}
