package com.example.grpc.client.grpcclient.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixUtils {

    /**
     * Checks whether given number is in power of 2
     * Reference: https://www.geeksforgeeks.org/program-to-find-whether-a-given-number-is-power-of-2/
     */
    private static boolean isPowerOfTwo(int n)
    {
        return (int)(Math.ceil((Math.log(n) / Math.log(2))))
                == (int)(Math.floor(((Math.log(n) / Math.log(2)))));
    }

    private static int[][] getMatrixA(List<String> list) {
        String[] size = list.get(0).split(" ");
        int row = Integer.parseInt(size[0]);
        int col = Integer.parseInt(size[1]);
        int skip = 3;
        return getMatrix(list, row, col, skip);
    }

    private static int[][] getMatrixB(List<String> list) {
        String[] sizeA = list.get(0).split(" ");
        int matArows = Integer.parseInt(sizeA[0]);
        int skip = matArows+4;

        String[] size = list.get(1).split(" ");
        int row = Integer.parseInt(size[0]);
        int col = Integer.parseInt(size[1]);
        return getMatrix(list, row, col, skip);
    }

    private static int[][] getMatrix(List<String> list, int row, int col, int skip) {

        //Checks if the matrix is square matrix and the size is of power of 2
        if(row != col || !isPowerOfTwo(row))
        {
            throw new RuntimeException("Invalid matrix size");
        }
        int[][] matrix = new int[row][col];

        for(int i=0; i< row; i++)
        {
            String[] rowVals = list.get(i+skip).split(" ");
            for(int j=0; j<col; j++)
            {
                matrix[i][j] = Integer.parseInt(rowVals[j]);
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        int row = matrix.length;
        int col = matrix[0].length;

        System.out.println();
        for(int i=0; i<row; i++)
        {
            for(int j=0; j<col; j++)
            {
                System.out.print(matrix[i][j]+" ");
            }
            System.out.println();
        }
    }

    public static String encodeMatrix(int[][] matrix) {
        return Arrays.deepToString(matrix);
    }

    public static int[][] decodeMatrix(String matrix){
        return stringToDeep(matrix);
    }

    /**
     * Converts array.toDeepString() back to 2D array
     * Reference: https://stackoverflow.com/questions/22377447/java-multidimensional-array-to-string-and-string-to-array/22428926#22428926
     */
    public static int[][] stringToDeep(String str) {
        int row = 0;
        int col = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '[') {
                row++;
            }
        }
        row--;
        for (int i = 0;; i++) {
            if (str.charAt(i) == ',') {
                col++;
            }
            if (str.charAt(i) == ']') {
                break;
            }
        }
        col++;

        int[][] out = new int[row][col];

        str = str.replaceAll("\\[", "").replaceAll("\\]", "");

        String[] s1 = str.split(", ");

        int j = -1;
        for (int i = 0; i < s1.length; i++) {
            if (i % col == 0) {
                j++;
            }
            out[j][i % col] = Integer.parseInt(s1[i]);
        }
        return out;
    }

    public static String[] encodeBlocks(int[][][] matrices)
    {
        String[] encodedMatrix = new String[4];
        for (int i=0; i<4; i++)
        {
            encodedMatrix[i] = encodeMatrix(matrices[i]);
        }
        return encodedMatrix;
    }

    /**
     * Splits the given matrix into blocks
     * Reference: https://qmplus.qmul.ac.uk/pluginfile.php/2561581/mod_resource/content/0/BlockMult.java
     */
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

    /**
     * Converts the block matrices back into a single matrix
     * Reference: https://qmplus.qmul.ac.uk/pluginfile.php/2561581/mod_resource/content/0/BlockMult.java
     */
    public static int[][] calResult(int[][] A, int[][] B, int[][] C, int[][] D, int MAX, int bSize)
    {
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


    public static int[][][] getMatrixFromFile(MultipartFile file) {
        List<String> list = new ArrayList<>();
        try {
            InputStream inputStream = file.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            list = br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[][] matA = getMatrixA(list);
        int[][] matB = getMatrixB(list);
        return new int[][][]{matA, matB};
    }
}