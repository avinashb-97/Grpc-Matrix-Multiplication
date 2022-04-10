package com.example.grpc.client.grpcclient;

import java.util.Arrays;
import java.util.List;

class MatrixUtils {

    /**
     * Checks whether given number is in power of 2
     * Source: https://www.geeksforgeeks.org/program-to-find-whether-a-given-number-is-power-of-2/
     */
    private static boolean isPowerOfTwo(int n)
    {
        return (int)(Math.ceil((Math.log(n) / Math.log(2))))
                == (int)(Math.floor(((Math.log(n) / Math.log(2)))));
    }

    public static int[][] getMatrixA(List<String> list) {
        String[] size = list.get(0).split(" ");
        int row = Integer.parseInt(size[0]);
        int col = Integer.parseInt(size[1]);
        int skip = 3;
        return getMatrix(list, row, col, skip);
    }

    public static int[][] getMatrixB(List<String> list) {
        String[] sizeA = list.get(0).split(" ");
        int matArows = Integer.parseInt(sizeA[0]);
        int skip = matArows+4;

        String[] size = list.get(1).split(" ");
        int row = Integer.parseInt(size[0]);
        int col = Integer.parseInt(size[1]);
        return getMatrix(list, row, col, skip);
    }

    public static int[][] getMatrix(List<String> list, int row, int col, int skip) {
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
     * Source: https://stackoverflow.com/questions/22377447/java-multidimensional-array-to-string-and-string-to-array/22428926#22428926
     */
    private static int[][] stringToDeep(String str) {
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

}