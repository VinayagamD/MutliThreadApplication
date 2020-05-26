package com.vinay.mutlithread.matrixgenerator;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringJoiner;

public class MatrixApplication {
    private static final String INPUT_FILE = "./build/out/matrices";
    private static final String OUTPUT_FILE = "./build/out/matrices_results.txt";
    private static final int N = 10;

    public static void main(String[] args) throws IOException {
        ThreadSafeQueue threadSafeQueue = new ThreadSafeQueue();
        File inputFile = new File(INPUT_FILE);
        File outputFile = new File(OUTPUT_FILE);

        MatricesReaderProducer matricesReader = new MatricesReaderProducer(new FileReader(INPUT_FILE), threadSafeQueue);
        MatricesMultiplierConsumer matricesConsumer = new MatricesMultiplierConsumer(new FileWriter(OUTPUT_FILE), threadSafeQueue);
        matricesReader.start();
        matricesConsumer.start();
    }

    private static class MatricesMultiplierConsumer extends Thread {
        private ThreadSafeQueue queue;
        private FileWriter writer;

        public MatricesMultiplierConsumer(FileWriter writer,ThreadSafeQueue queue) {
            this.writer = writer;
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true){
                MatricesPair matricesPair = queue.remove();
                if (matricesPair == null){
                    System.out.println("No more matrices to read. Producer Thread is terminating");
                    break;
                }

                float [] [] result = multiplyMatrices(matricesPair.matrix1, matricesPair.matrix2);
                try {
                    saveMatrixToFile(writer,result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void saveMatrixToFile(FileWriter writer, float[][] matrix) throws IOException {
            for (int r = 0; r < N; r++) {
                StringJoiner stringJoiner = new StringJoiner(", ");
                for (int c = 0; c < N; c++) {
                    stringJoiner.add(String.format("%.2f", matrix[r][c]));
                }
                writer.write(stringJoiner.toString());
                writer.write("\n");
            }
            writer.write("\n");
        }

        private float[][] multiplyMatrices(float[][] m1, float[][] m2){
            float [][] result = new float[N][N];
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    for (int k = 0; k < N; k++) {
                        result[r][c] += m1[r][k] * m2[k][c];
                    }
                }
            }
            return result;
        }
    }

    private static class MatricesReaderProducer extends Thread {
        private Scanner scanner;
        private ThreadSafeQueue queue;

        public MatricesReaderProducer(FileReader fileReader, ThreadSafeQueue queue) {
            this.scanner = new Scanner(fileReader);
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                float[][] matrix1 = readMatrix();
                float[][] matrix2 = readMatrix();
                if(matrix1 == null || matrix2 == null){
                    queue.terminate();
                    System.out.println("No more matrices to read. Producer Thread is terminating");
                    return;
                }
                MatricesPair matricesPair = new MatricesPair();
                matricesPair.matrix1 = matrix1;
                matricesPair.matrix2 = matrix2;

                queue.add(matricesPair);
            }
        }

        private float[][] readMatrix(){
            float[][] matrix = new float[N][N];
            for (int r = 0; r < N; r++) {

                    if(!scanner.hasNext()){
                        return null;
                    }
                    String [] line = scanner.nextLine().split(",");
                    for (int c = 0; c < N; c++) {
                        matrix[r][c] = Float.valueOf(line[c]);
                    }

            }
            scanner.nextLine();
            return  matrix;
        }
    }

    private static class ThreadSafeQueue {
        private Queue<MatricesPair> queue = new LinkedList<>();
        private boolean isEmpty = true;
        private boolean isTerminate = false;
        public static final int CAPACITY = 5;

        public synchronized void add(MatricesPair matricesPair){
            while (queue.size() == CAPACITY){
                try {
                    wait();
                }catch (InterruptedException e){
                }
            }
            queue.add(matricesPair);
            isEmpty = false;
            notify();
        }

        public  synchronized MatricesPair remove(){
            MatricesPair matricesPair = null;
            while (isEmpty && !isTerminate){
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            if(queue.size() == 1){
                isEmpty = true;
            }

            if(queue.size() == 0  & isTerminate){
                return null;
            }
            System.out.println("queue size "+queue.size());
            matricesPair = queue.remove();
            if(queue.size() == CAPACITY - 1){
                notifyAll();
            }
            return matricesPair;
        }

        public synchronized void  terminate(){
            isTerminate = true;
            notifyAll();
        }

    }

    private static class MatricesPair {
        public float [] [] matrix1;
        public float [] [] matrix2;


    }
}
