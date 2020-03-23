package com.vinay.mutlithread.terminating;

public class ExampleHandledInterruptMethod {

    public static void main(String[] args) {
        Thread thread = new Thread(new BlockingTask());
        thread.start();

        thread.interrupt();
    }

    private static class BlockingTask implements Runnable {

        @Override
        public void run() {
            // do things
            try {
                Thread.sleep(500000);
            } catch (InterruptedException e) {
                System.out.println("Exiting blocking thread");
            }
        }
    }
}
