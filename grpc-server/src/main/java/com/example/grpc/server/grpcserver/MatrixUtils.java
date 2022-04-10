package com.example.grpc.server.grpcserver;

import java.util.Arrays;

class MatrixUtils {

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

}