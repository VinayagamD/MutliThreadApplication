package com.vinay.mutlithread.memory.management.stack;

public class StackAndHeapRegion {

    public static void main(String[] args) {
        int x = 1;
        int y = 2;
        int result = sum(x,y);
    }

    private static int sum(int a, int b) {
        int s = a+b;
        return s;
    }
}
