package com.vinay.mutlithread.join;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ExampleJoinMain {

    public static void main(String[] args) throws InterruptedException{
        List<Long> inputNumbers = Arrays.asList(1000000000L, 3435L, 35435L, 2324L, 4656L, 23L, 2435L, 5566L);
        // We want to calculate the !0, !3435, !35435, !2324, !4656, !23, !2435, !5566
        List<FactorialThread> threads = new ArrayList<>();
        inputNumbers.forEach(input -> {
            threads.add(new FactorialThread(input));
        });
        threads.forEach(thread ->{
            thread.setDaemon(true);
            thread.start();
        });
        threads.forEach(factorialThread -> {
            try {
                factorialThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        threads.forEach(thread -> {
            if(thread.isFinished()){
                System.out.println("Factorial of "+ inputNumbers.get(threads.indexOf(thread)) + " is " + thread.getResult());
            } else  {
                System.out.println("Factorial of "+ inputNumbers.get(threads.indexOf(thread)) + " is still in progress");
            }
        });

    }



    public static class FactorialThread extends Thread {

        private long inputNumber;
        private BigInteger result = BigInteger.ZERO;
        private boolean isFinished = false;

        public FactorialThread(long inputNumber) {
            this.inputNumber = inputNumber;
        }

        @Override
        public void run() {
            this.result = factorial(inputNumber);
            this.isFinished = true;
        }

        public BigInteger factorial(long n){
            BigInteger tempResult = BigInteger.ONE;
            for (long i = n; i > 0 ; i--) {
                tempResult = tempResult.multiply(new BigInteger(Long.toString(i)));
            }
            return tempResult;
        }

        public BigInteger getResult() {
            return result;
        }

        public boolean isFinished() {
            return isFinished;
        }
    }

}
