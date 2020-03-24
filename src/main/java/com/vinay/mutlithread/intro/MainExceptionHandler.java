package com.vinay.mutlithread.intro;

public class MainExceptionHandler {

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            // Code that will run in a new thread
            throw new RuntimeException("Intentional Exception");
        });
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.out.println("A critical error happened in thread "+t.getName()
                    + " the error is " + e.getMessage());
        });

        thread.start();
    }
}
